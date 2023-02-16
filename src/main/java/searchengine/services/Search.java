package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.IndexRank;
import searchengine.config.SearchResult;
import searchengine.services.materializer.Materialize;
import searchengine.model.*;
import searchengine.repository.*;
import searchengine.services.materializer.SnippetBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

public class Search {
    private final Materialize materialize;
    private IndexRepository indexRepository;
    private FieldRepository fieldRepository;
    private PageRepository pageRepository;
    private static final String TAGS_SCRIPT = "(<script.*>).*|\\s*(</script>)";
    private static final String REG_ALL_TAGS = "/</?[\\w\\s]*>|<.+[\\W]>/g";//"<!?\\/?[a-z\\s\"0-9=_]*>";
    private static final int SNIPPET_INTERVAL = 5;
    private static final int SNIPPET_MAX_LENGTH = 200;
    public Search(LemmaRepository lemmaRepository) { materialize = new Materialize(lemmaRepository);}
    public searchengine.config.Search search(String text, Site site, PageRepository pageRepository, IndexRepository indexRepository,
                                             FieldRepository fieldRepository, SiteRepository siteRepository) {
        this.indexRepository = indexRepository;
        this.fieldRepository = fieldRepository;
        this.pageRepository = pageRepository;

        Set<SearchResult> searchResults = new TreeSet<>(Comparator.comparing(SearchResult::getRelevance));

        try {
            if (site == null) {
                siteRepository.findAll().forEach(s -> {
                    try {
                        searchResults.addAll(searchingBySite(s, text));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                searchResults.addAll(searchingBySite(site, text));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        searchengine.config.Search search = new searchengine.config.Search();

        search.setCount(searchResults.size());
        search.setResult(true);
        search.setData(searchResults);

        return search;
    }

    private Set<SearchResult> searchingBySite(Site site, String text) throws IOException {
        List<Page> pages = pageRepository.findAllBySite(site);
        return addSearchQuery(text, site, pages);
    }
    private Set<SearchResult> addSearchQuery(String text, Site site, List<Page> pages) {
        SortedSet<Lemma> lemmas = new TreeSet<>();

        for (String word : text.split(" ")) {
            Lemma lemma = materialize.getLemma(word.toLowerCase(Locale.ROOT), site);
            if (lemma != null) {
                lemmas.add(lemma);
            }
        }
        List<IndexRank.IndexRanks> indexRanks = getIndexRanks(lemmas, pages);
        return getSearchResults(indexRanks, lemmas, site);
    }

    private List<IndexRank.IndexRanks> getIndexRanks(SortedSet<Lemma> lemmas, List<Page> pages) {
        List<IndexRank.IndexRanks> indexRanks = new ArrayList<>();

        for (Lemma lemma : lemmas) {
            int count = 0;
            while (pages.size() > count) {
                Index index = indexRepository.findByLemmaAndPage(lemma, pages.get(count));
                if (index == null) {
                    pages.remove(count);
                } else {
                    IndexRank.IndexRanks indexRank = new IndexRank.IndexRanks();
                    indexRank.setPage(pages.get(count));
                    indexRank.setRanks(lemma.getLemma(), index.getLemmaRank());
                    indexRank.setRAbs();

                    indexRanks.add(indexRank);
                    count++;
                }
            }
        }

        for (IndexRank.IndexRanks indexRank : indexRanks) {
            indexRank.setRRel();
        }
        return indexRanks;
    }
    private void resultToList(Set<SearchResult> searchResults, Document document, IndexRank.IndexRanks indexRank,
                              AtomicReference<String> snippet, Site site) {
        SearchResult sResult = new SearchResult();

        sResult.setTitle(document.title());
        sResult.setRelevance(indexRank.getRRel());
        sResult.setSnippet(snippet.get());
        sResult.setUrl(indexRank.getPage().getPath().replace(site.getUrl(), ""));
        sResult.setSite(site.getUrl());
        sResult.setSiteName(site.getName());

        searchResults.add(sResult);
    }

    private Set<SearchResult> getSearchResults(List<IndexRank.IndexRanks> indexRanks,
                                               SortedSet<Lemma> lemmas, Site site) {
        Set<SearchResult> searchResults = new TreeSet<>(Comparator.comparing(SearchResult::getRelevance));
        List<Field> fields = fieldRepository.findAll();

        for (IndexRank.IndexRanks indexRank : indexRanks){
            Document document = Jsoup.parse(indexRank.getPage().getContent());

            AtomicReference<String> snippet = new AtomicReference<>("");
            AtomicInteger maxSnippet = new AtomicInteger();
            AtomicBoolean isHaven = new AtomicBoolean(false);
            StringBuilder snippetBuilder = new StringBuilder();
            Set<String> wordsFound = new TreeSet<>();

            for (Lemma lem : lemmas.stream().toList()) {
                int count = 0;
                String l = lem.getLemma();

                for (Field field : fields) {
                    String text = document.select(field.getSelector()).text().toLowerCase();
                    String str = text.toLowerCase();
                    List<String> words = Materialize.getSeparateWordsList(str);

                    for (int i = 0; i < words.size(); i++) {

                        if (Materialize.legitimatize(words.get(i)).containsKey(l)) {
                            count++;
                            wordsFound.add(words.get(i).replaceAll("[^a-zA-Zа-яА-ЯёЁ]", ""));
                            int start = Math.max(i - SNIPPET_INTERVAL, 0);
                            int end = Math.min(i + SNIPPET_INTERVAL, words.size());
//                            int totalLength = snippetBuilder.length() + words.subList(start, end)
//                                    .stream().flatMapToInt(s -> IntStream.of(s.length())).sum();
//                            if (totalLength >= SNIPPET_INTERVAL) {
//                                end = i;
//                            }
                            snippetBuilder.append(start > 0 ? "..." : "");
                            words.subList(start, end).forEach(w -> { snippetBuilder.append(w).append(" "); });
                            if (snippetBuilder.length() > SNIPPET_MAX_LENGTH) {
                                break;
                            }
                            snippetBuilder.append(end < words.size() ? "..." : "");
                        }

                        String snipt = snippetBuilder.toString();

                        if (snipt.length() > 0) {
                            for (String word : wordsFound) {
                                snipt = snipt.replaceAll(word, "<b>" + word + "</b>");
                            }
                        }

                        if (count > maxSnippet.get()) {
                            snippet.set(snipt);
                            maxSnippet.set(count);
                            isHaven.set(true);
                        }
                    }
                }
                if (count == 0) {
                    lemmas.remove(lem);
                }
            }

            if (isHaven.get()) {
                resultToList(searchResults, document, indexRank, snippet, site);
            }
        }
        return searchResults;
    }
}
