package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Search;
import searchengine.config.SearchResult;
import searchengine.config.SitesList;
import searchengine.config.Status;
import searchengine.lemmatizator.Materialize;
import searchengine.model.*;
import searchengine.parse.NodeLink;
import searchengine.parse.TransitionLink;
import searchengine.repository.*;
import searchengine.search.SearchText;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
@Service
@RequiredArgsConstructor
public class Storage {
    private static final int THREADS = 3;
    private static final Map<String, Float> fields = new HashMap<>(
                                        Map.of("title", 1.0F, "body", 0.8F));
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
    private final SitesList sites;
    private List<Thread> threads;
    private List<ForkJoinPool> forkJoinPools;
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

    public void initFields(){
        for (String str : fields.keySet()) {
            Field field = new Field();
            field.setName(str);
            field.setSelector(str);
            field.setWeight(fields.get(str));
            fieldRepository.save(field);
        }
    }
    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        clearData();
        Materialize.setLemmaRepository(lemmaRepository);

        List<TransitionLink> parses = new ArrayList<>();
        List<searchengine.config.Site> siteList = sites.getSites();
        initFields();

        for (searchengine.config.Site valueSite : siteList) {
            String mainPage = valueSite.getUrl();

            Site site = siteRepository.findSiteByUrl(mainPage);

            if (site == null) {
                site = new Site();
            }

            site.setUrl(mainPage);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXING);
            site.setError("");
            site.setName(valueSite.getName());
            NodeLink nodeLink = new NodeLink(site.getUrl());

            parses.add(transition(site, nodeLink));
            siteRepository.save(site);
        }

        siteList.clear();

        for (TransitionLink parse : parses) {
        threads.add(new Thread(() -> {
            Site site = parse.getSite();

            try {

                ForkJoinPool forkJoinPool = new ForkJoinPool(THREADS);

                forkJoinPools.add(forkJoinPool);

                forkJoinPool.execute(parse);
                int count = parse.join();
                System.out.println(count);

                site.setStatus(Status.INDEXED);
                siteRepository.save(site);

            } catch (CancellationException ex) {
                ex.printStackTrace();
                site.setError("Error: " + ex.getMessage());
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
            }
        }));
        }

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

        initFields();
        NodeLink nodeLink = new NodeLink(url);
        if (siteList.size() == 0) {
            List<searchengine.config.Site> configSites = sites.getSites();

            for (searchengine.config.Site configSite : configSites) {
                if (url.contains(configSite.getUrl()) &&
                        pageRepository.findByPath(nodeLink.parseOneLink(nodeLink.getLink())) == null) {
                    String mainPage = configSite.getUrl();
                    Site site = new Site();

                    site.setUrl(mainPage);
                    site.setStatusTime(new Date());
                    site.setStatus(Status.INDEXING);
                    site.setError("");
                    site.setName(configSite.getName());
                    siteRepository.save(site);

                    NodeLink node = new NodeLink(site.getUrl());
                    TransitionLink parse = new TransitionLink(node, site.getUrl(), site,
                            fieldRepository, siteRepository, indexRepository,
                            pageRepository, lemmaRepository);
                    try {
                        parse.addPage(url);
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
                if (url.contains(site.getUrl()) &&
                        pageRepository.findByPath(nodeLink.parseOneLink(nodeLink.getLink())) == null) {
                    site.setStatus(Status.INDEXING);
                    siteRepository.save(site);

                    NodeLink node = new NodeLink(site.getUrl());
                    TransitionLink parse = new TransitionLink(node, site.getUrl(), site,
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

    public Search search(String query, String site, int offset, int limit) throws IOException {

        SearchText searchText = new SearchText();

        Search search = searchText.search(query, siteRepository.findSiteByUrl(site), pageRepository,
                indexRepository, fieldRepository, siteRepository);

        if (search.getCount() < offset) {
            return new Search();
        }

        if (search.getCount() > limit) {
            Set<SearchResult> searchResults = new TreeSet<>(Comparator.comparing(SearchResult::getRelevance));

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
