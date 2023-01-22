package searchengine.services.parse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.services.materializer.Materialize;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.repository.FieldRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TransitionLink extends RecursiveTask<Integer> {
    private final static Log log = LogFactory.getLog(TransitionLink.class);
    private final NodeLink nodeLink;
    private String startPath;
    private final String mainPath;
    private static boolean isStopping;
    private static final Map<String, Float> fields = new HashMap<>();
    private static final ConcurrentHashMap<String, Lemma> lemmasMap = new ConcurrentHashMap<>();
    private final Materialize materialize;
    private static PageRepository pageRepository;
    private static IndexRepository indexRepository;
    private static SiteRepository siteRepository;
    private static LemmaRepository lemmaRepository;
    private final Site site;
    public TransitionLink(NodeLink nodeLink, String sitePath, Site site, FieldRepository fieldRepository,
                          SiteRepository siteRepository, IndexRepository indexRepository,
                          PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this.startPath = sitePath;
        this.nodeLink = nodeLink;

        this.mainPath = sitePath;
        materialize = new Materialize(lemmaRepository);

        fieldRepository.findAll().forEach(it ->
                TransitionLink.fields.put(it.getName(), it.getWeight())
        );

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

    public TransitionLink(NodeLink nodeLink, String startPath, Site site, String mainPath) {
        this.nodeLink = nodeLink;
        this.startPath = startPath;
        materialize = new Materialize(lemmaRepository);

        this.mainPath = mainPath;
        this.site = site;
    }

    @Override
    protected Integer compute() {

            if (!startPath.endsWith("/")) {
                startPath += "/";
            }

            Set<String> links = Collections.synchronizedSet(nodeLink.parseLink(startPath));
            Set<NodeLink> nodeLinkSet = new CopyOnWriteArraySet<>();
            try {
                for (String link : links) {

                    if (pageRepository.findByPath(link) == null && !isStopping()) {
                        Document document = Jsoup.connect(mainPath + link).get();
                        addPage(document, link);
                        NodeLink nodeLink = new NodeLink(mainPath + link, document);
                        log.info(mainPath + link);
                        nodeLinkSet.add(nodeLink);
                    }
                }

            } catch (IOException | NullPointerException exception) {
                exception.fillInStackTrace();
            }

            List<TransitionLink> listTask = new ArrayList<>();
            for (NodeLink node : nodeLinkSet) {
                if (node.getLink().contains(mainPath) && !node.getLink().contains("#")) {
                    TransitionLink task = new TransitionLink(node, node.getLink(), site, mainPath);
                    task.fork();
                    listTask.add(task);
                }
            }
        return addResultsFromTasks(listTask);
    }

    public void addPage(String link)  {
        try {
            addPage(Jsoup.connect(link).get(), link.replaceAll(site.getUrl(), ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPage(Document doc, String link) {
        Page page = new Page();
        int code = doc.connection().response().statusCode();
        page.setCode(code);
        page.setPath(link);
        page.setContent(doc.html());
        page.setSite(site);

        pageRepository.save(page);

        if (code < 400) {
            addLemmas(doc, page);
        }
    }
    public void addLemmas(Document doc, Page page) {

        ConcurrentHashMap<String, Index> indices = new ConcurrentHashMap<>();
        Lemma oldLemma;
        for (String key : fields.keySet()) {
            Map<String, Integer> words = Materialize.legitimatize(doc.select(key).text());
            for (String word : words.keySet()) {
                oldLemma = lemmasMap.get(word);

                if (oldLemma == null) {
                    oldLemma = new Lemma();
                    oldLemma.setLemma(word);
                    oldLemma.setFrequency(1);
                    oldLemma.setSite(site);
                } else {
                    int freq = oldLemma.getFrequency();
                    oldLemma.setFrequency(freq + 1);
                }
                lemmaRepository.save(oldLemma);
                lemmasMap.put(word, oldLemma);

                addIndex(oldLemma, page, words, word, indices, key);
            }
        }
    }
    public void addIndex(Lemma lemma, Page page, Map<String, Integer> map,
                         String word, ConcurrentHashMap<String, Index> indices, String key) {
        Index index = indices.get(word);
        if (index == null) {
            Index ind = new Index();
            ind.setLemma(lemma);
            ind.setPage(page);
            ind.setLemmaRank(map.get(word) * fields.get(key));
            indexRepository.save(ind);
            indices.put(word, ind);
        } else {
            float rank = index.getLemmaRank();
            index.setLemmaRank(rank + map.get(word) * fields.get(key));
            indexRepository.save(index);
        }
    }

    private int addResultsFromTasks(List<TransitionLink> tasks) {
        int count = 0;
            for (TransitionLink item : tasks) {
                count += item.join();
            }
            return count;
    }
    public Site getSite() {
        return site;
    }

    public void onStartIndexing() {
        isStopping = false;
    }
    public void offStartIndexing() {
        isStopping = true;
    }

    public static boolean isStopping() {
        return isStopping;
    }

    public static void removeDataFromLemmasMap() {
        lemmasMap.clear();
    }
}