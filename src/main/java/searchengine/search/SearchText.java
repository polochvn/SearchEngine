package searchengine.search;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.config.IndexRank;
import searchengine.config.Search;
import searchengine.config.SearchResult;
import searchengine.lemmatizator.Materialize;
import searchengine.model.*;
import searchengine.repository.FieldRepository;
import searchengine.repository.IndexRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SearchText {
        private final Materialize lemmatizer;
        private IndexRepository indexRepository;
        private FieldRepository fieldRepository;
        private PageRepository pageRepository;
        public SearchText() {
                lemmatizer = new Materialize();
        }
        public Search search(String text, Site site, PageRepository pageRepository, IndexRepository indexRepository,
                             FieldRepository fieldRepository, SiteRepository siteRepository) throws IOException {
                this.indexRepository = indexRepository;
                this.fieldRepository = fieldRepository;
                this.pageRepository = pageRepository;

                Set<SearchResult> searchResults = new TreeSet<>(Comparator.comparing(SearchResult::getRelevance));

                if (site == null) {
                        siteRepository.findAll().forEach(s -> {
                                try {
                                        searchResults.addAll(searchingBySite(s, text));
                                } catch (IOException e) {
                                        throw new RuntimeException(e);
                                }
                        });
                } else {
                        searchResults.addAll(searchingBySite(site, text));
                }

                Search search = new Search();

                search.setCount(searchResults.size());
                search.setResult(true);
                search.setData(searchResults);

                return search;
        }

        private Set<SearchResult> searchingBySite(Site site, String text) throws IOException {
                List<Page> pages = pageRepository.findAllBySite(site);
                return addSearchQuery(text, site, pages);
        }

        private Set<SearchResult> addSearchQuery(String text, Site site, List<Page> pages) throws IOException {
                SortedSet<Lemma> lemmas = new TreeSet<>();

                for (String word : text.split(" ")) {
                        Lemma lemma = lemmatizer.getLemma(word.toLowerCase(Locale.ROOT), site);
                        if (lemma != null) {
                                lemmas.add(lemma);
                        }
                }
                List<IndexRank.IndexRanks> indexRanks = getIndexRanks(lemmas, pages);
                return getSearchResults(indexRanks, lemmas, site);
        }

        private List<IndexRank.IndexRanks> getIndexRanks(SortedSet<Lemma> lemmas, List<Page> pages) {
                List<IndexRank.IndexRanks> indexRanks = new ArrayList<>();

                for (Lemma lemma : lemmas){
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

        private Set<SearchResult> getSearchResults(List<IndexRank.IndexRanks> indexRanks,
                                                         SortedSet<Lemma> lemmas, Site site) {
                Set<SearchResult> searchResults = new TreeSet<>(Comparator.comparing(SearchResult::getRelevance));
                List<Field> fields = fieldRepository.findAll();

                for (IndexRank.IndexRanks indexRank : indexRanks){
                        Document document = Jsoup.parse(indexRank.getPage().getContent());

                        AtomicReference<String> snippet = new AtomicReference<>("");
                        AtomicInteger maxSnippet = new AtomicInteger();
                        SearchResult sResult = new SearchResult();
                        AtomicBoolean isHaven = new AtomicBoolean(false);

                        for (Field field : fields){

                                document.select(field.getSelector()).forEach(i -> {
                                        String str = i.text().toLowerCase();
                                        int count = 0;
                                        for (Lemma lem : lemmas.stream().toList()) {
                                                String l = lem.getLemma();
                                                if (str.contains(l)) {
                                                        count++;
                                                        str = str.replaceAll("(?i)" + l,
                                                                "<b>" + l + "</b>");
                                                } else {
                                                        lemmas.remove(lem);
                                                }
                                        }

                                        if (count > maxSnippet.get()) {
                                                snippet.set(str);
                                                maxSnippet.set(count);
                                                isHaven.set(true);
                                        }
                                });
                        }

                        if (isHaven.get()) {
                                sResult.setTitle(document.title());
                                sResult.setRelevance(indexRank.getRRel());
                                sResult.setSnippet(snippet.get());
                                sResult.setUrl(indexRank.getPage().getPath().replace(site.getUrl(), ""));
                                sResult.setSite(site.getUrl());
                                sResult.setSiteName(site.getName());

                                searchResults.add(sResult);
                        }
                }
                return searchResults;
        }
}
