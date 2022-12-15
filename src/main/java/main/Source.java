package main;


import main.entities.*;
import main.repository.*;
import main.parse.NodeLink;
import main.parse.TransitionLink;
import main.search.SearchText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
@Service
public class Source {
    private static final int THREADS = 3;
    @Autowired
    private FieldRepository fieldRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private IndexRepository indexRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private Config config;
    private List<Thread> threads = new ArrayList<>();
    private List<ForkJoinPool> forkJoinPools = new ArrayList<>();
    private void clearData() {
        indexRepository.deleteAll();
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }

    private TransitionLink transition(Site site, NodeLink nodeLink) {
        return new TransitionLink(nodeLink, site.getUrl(), site, fieldRepository, siteRepository,
                                                indexRepository, pageRepository, lemmaRepository);
    }

    public ResStatistics getStatistic() {
        Statistic statistic = new Statistic();

        AtomicInteger allLemmas = new AtomicInteger();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();

        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            return new ResStatistics();
        }

        for(Site s : siteList){
            int pages = pageRepository.countBySite(s);
            int lemmas = lemmaRepository.countBySite(s);

            s.setPages(pages);
            s.setLemmas(lemmas);
            statistic.addDetailed(s);

            allPages.updateAndGet(v -> v + pages);
            allLemmas.updateAndGet(v -> v + lemmas);
            allSites.getAndIncrement();
        }

        Total total = new Total();
        total.setIndexing(true);
        total.setLemmas(allLemmas.get());
        total.setPages(allPages.get());
        total.setSites(allSites.get());

        statistic.setTotal(total);

        ResStatistics statistics = new ResStatistics();

        statistics.setResult(true);
        statistics.setStatistics(statistic);

        return statistics;
    }

    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        clearData();

        List<TransitionLink> parses = new ArrayList<>();
        List<String> urls = config.getSitesUrl();
        List<String> namesUrls = config.getSitesName();


        for (int i = 0; i < urls.size(); ++i) {
            String mainPage = urls.get(i);

            Site site = siteRepository.findSiteByUrl(mainPage);

            if (site == null) {
                site = new Site();
            }

            site.setUrl(mainPage);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXING);
            site.setName(namesUrls.get(i));
            NodeLink nodeLink = new NodeLink(site.getUrl());

            parses.add(transition(site, nodeLink));
            siteRepository.save(site);
        }

        urls.clear();
        namesUrls.clear();


        parses.forEach(parse -> threads.add(new Thread(() -> {
            Site site = parse.getSite();

            try {
                site.setStatus(Status.INDEXING);
                siteRepository.save(site);

                ForkJoinPool forkJoinPool = new ForkJoinPool(THREADS);

                forkJoinPools.add(forkJoinPool);

                forkJoinPool.execute(parse);
                int count = parse.join().size();

                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
            } catch (CancellationException ex) {
                ex.printStackTrace();
                site.setError("Error: " + ex.getMessage());
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
            }
        })));

        threads.forEach(Thread::start);
        forkJoinPools.forEach(ForkJoinPool::shutdown);
    }

    public boolean startIndexing() {
        AtomicBoolean isIndexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                isIndexing.set(true);
            }
        });

        if (isIndexing.get()) {
            return true;
        }
        new Thread(this::indexing).start();

        return false;
    }

    public boolean stopIndexing() {
        AtomicBoolean isIndexing = new AtomicBoolean(false);

        siteRepository.findAll().forEach(site -> {
            if (site.getStatus().equals(Status.INDEXING)) {
                isIndexing.set(true);
            }
        });

        if (!isIndexing.get()) {
            return true;
        }

        forkJoinPools.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);

        siteRepository.findAll().forEach(site -> {
            site.setError("Stop Indexing!");
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        });

        threads.clear();
        forkJoinPools.clear();

        return false;
    }

    public boolean indexPage(String url) {
        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) {
            List<String> urls = config.getSitesUrl();
            List<String> namesUrls = config.getSitesName();

            for (int i = 0; i < urls.size(); ++i) {
                if (url.contains(urls.get(i))) {
                    String mainPage = urls.get(i);
                    Site site = new Site();

                    site.setUrl(mainPage);
                    site.setStatusTime(new Date());
                    site.setStatus(Status.INDEXING);
                    site.setName(namesUrls.get(i));
                    siteRepository.save(site);
                    NodeLink nodeLink = new NodeLink(site.getUrl());

                    TransitionLink parse = new TransitionLink(nodeLink, site.getUrl(), site,
                                                fieldRepository, siteRepository, indexRepository,
                                                            pageRepository, lemmaRepository);
                    try {
                        parse.addPage(site.getUrl());
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        } else {
            for (Site site : siteList) {
                if (url.contains(site.getUrl())) {
                    site.setStatus(Status.INDEXING);
                    siteRepository.save(site);
                    NodeLink nodeLink = new NodeLink(site.getUrl());

                    TransitionLink parse = new TransitionLink(nodeLink, site.getUrl(), site,
                                            fieldRepository, siteRepository, indexRepository,
                                                        pageRepository, lemmaRepository);
                    try {
                        parse.addPage(site.getUrl());
                    } catch (IOException e) {
                        return false;
                    }

                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);

                    return true;
                }
            }
        }

        return false;
    }

    public Search search(String query, String site, int offset, int limit) {

        SearchText searchText = new SearchText();

        Search search = searchText.search(query, siteRepository.findSiteByUrl(site), pageRepository,
                indexRepository, fieldRepository, siteRepository);

        if (search.getCount() < offset) {
            return new Search();
        }

        if (search.getCount() > limit) {
            Set<SearchResult> searchResults = new TreeSet<>();

            search.getData().forEach(it -> {
                if (searchResults.size() <= limit) {
                    searchResults.add(it);
                }
            });

            search.setData(searchResults);
        }

        return search;
    }
}
