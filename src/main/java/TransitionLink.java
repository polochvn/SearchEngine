import entities.*;
import lemmatizator.Lemmatizer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import repository.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class TransitionLink extends RecursiveTask<Set<String>> {

    private NodeLink nodeLink;
    private String sitePath;
    private static Set<String> allLinks = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, Lemma> lemmasMap = new ConcurrentHashMap<>();
    private static PageRepository pageRepository;
    private static IndexRepository indexRepository;
    private static SiteRepository siteRepository;
    private static LemmaRepository lemmaRepository;
    private Site site;
    private static ConcurrentHashMap<String, Index> indices = new ConcurrentHashMap<>();
    private static final Map<String, Float> TAGS = Map.of("title", 1F, "body", 0.8F);

    public TransitionLink(NodeLink nodeLink, String sitePath, Site site, FieldRepository fieldRepository,
                          SiteRepository siteRepository, IndexRepository indexRepository,
                          PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this.nodeLink = nodeLink;
        this.sitePath = sitePath;

        if (TransitionLink.indexRepository == null) {
            TransitionLink.indexRepository = indexRepository;
        }

        if (TransitionLink.pageRepository == null) {
            TransitionLink.pageRepository = pageRepository;
        }

        if (TransitionLink.siteRepository == null) {
            TransitionLink.siteRepository = siteRepository;
        }

        if (TransitionLink.lemmaRepository == null) {
            TransitionLink.lemmaRepository = lemmaRepository;
        }

        this.site = site;
    }

    public TransitionLink(NodeLink nodeLink, String sitePath, Site site) {
        this.nodeLink = nodeLink;
        this.sitePath = sitePath;
        this.site = site;
    }

    @Override
    protected Set<String> compute() {
        Set<String> links = Collections.synchronizedSet(nodeLink.parseLink(nodeLink.getLink()));
        Set<NodeLink> nodeLinkSet = new CopyOnWriteArraySet<>();

        try {
        for (String link : links) {
            if (!checkSet(link, allLinks)) {
                    Connection.Response response = Jsoup.connect(sitePath + link)
                        .get().connection().response();
                    Document document = response.parse();
                    addPage(response, document, link);
                NodeLink nodeLink = new NodeLink(sitePath + link);
                nodeLinkSet.add(nodeLink);
                allLinks.add(link);
            }
        }
        nodeLink.setNodeLinkSet(nodeLinkSet);
        } catch (IOException | NullPointerException exception) {
            site.setError("Остановка индексации");
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        }

        List<TransitionLink> listTask = new ArrayList<>();
        for (NodeLink node : nodeLinkSet) {
            TransitionLink task = new TransitionLink(node, sitePath, site);
            task.fork();
            listTask.add(task);
        }
        addResultsFromTasks(links, listTask);
        return links;
    }

    public void addPage(String link) throws IOException {
        Connection.Response response = Jsoup.connect(link).get().connection().response();

        addPage(response, response.parse(), link);
    }

    public void addPage(Connection.Response response, Document doc, String link) {
        Page page = new Page();
        page.setCode(response.statusCode());
        page.setPath(link);
        page.setContent(doc.html());
        page.setSite(site);

        pageRepository.save(page);

        if (response.statusCode() < 400) {
            addLemmas(doc, page);
        }
    }
    public void addLemmas(Document doc, Page page) {
        HashSet<Lemma> lemmas = new HashSet<>();
        Map<String, Integer> wordsFromTitlePage = Lemmatizer.lemmatize(doc.title());
        Map<String, Integer> wordsFromBodyPage = Lemmatizer.lemmatize(doc.body().text());
        Lemma oldLemma;
        for (String word : wordsFromTitlePage.keySet()) {
            oldLemma = lemmasMap.get(word);

            if (oldLemma == null) {
                oldLemma = new Lemma();

                oldLemma.setLemma(word);
                oldLemma.setFrequency(1);
                oldLemma.getPages().add(page);

            } else {
                int freq = oldLemma.getFrequency();
                oldLemma.setFrequency(freq + 1);
                oldLemma.getPages().add(page);
            }
            lemmaRepository.save(oldLemma);
            lemmas.add(oldLemma);
            lemmasMap.put(word, oldLemma);

            addIndex(oldLemma, page, wordsFromTitlePage, word, doc);
        }

        for (String word : wordsFromBodyPage.keySet()) {
            oldLemma = lemmasMap.get(word);

            if (oldLemma == null) {
                oldLemma = new Lemma();

                oldLemma.setLemma(word);
                oldLemma.setFrequency(1);
                oldLemma.getPages().add(page);
            } else if (!wordsFromTitlePage.containsKey(word)){
                int freq = oldLemma.getFrequency();
                oldLemma.setFrequency(freq + 1);
                oldLemma.getPages().add(page);

                addIndex(oldLemma, page, wordsFromTitlePage, word, doc);
            }
            lemmas.add(oldLemma);
            lemmasMap.put(word, oldLemma);
            lemmaRepository.save(oldLemma);

            if (indices.containsKey(word)){
                float rank = indices.get(word).getRank();
                indices.get(word).setRank(rank + wordsFromBodyPage.get(word)
                        * TAGS.get(doc.body().tagName()));
            }
            page.setLemmaSet(lemmas);
        }
    }
    public void addIndex(Lemma lemma, Page page, Map<String, Integer> map,
                         String word, Document doc) {
        Index index = new Index();
        index.setLemma(lemma);
        index.setPage(page);
        index.setRank(map.get(word) * TAGS.get(doc.tagName("title").tagName()));
        indexRepository.save(index);
        indices.put(word, index);

    }

    private void addResultsFromTasks(Set<String> set, List<TransitionLink> tasks) {
            for (TransitionLink item : tasks) {
                set.addAll(item.join());
            }
    }

    private boolean checkSet(String str, Set<String> set) {
        if (!(set == null)) {
            for (String link : set) {
                if (link.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ConcurrentHashMap<String, Lemma> getLemmasMap() {
        return lemmasMap;
    }

    public static ConcurrentHashMap<String, Index> getIndices() {
        return indices;
    }

    public static Set<String> getAllLinks() {
        return allLinks;
    }

    public Site getSite() {
        return site;
    }
}