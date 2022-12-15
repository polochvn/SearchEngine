package main.search;

import main.entities.*;
import main.lemmatizator.Lemmatizer;
import main.parse.TransitionLink;
import main.repository.FieldRepository;
import main.repository.IndexRepository;
import main.repository.PageRepository;
import main.repository.SiteRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SearchText {

        private IndexRepository indexRepository;
        private FieldRepository fieldRepository;
        private PageRepository pageRepository;
        private static Map<String, Lemma> lemmaMap = new HashMap<>();

        private static final Double PERCENTAGE_WORDS = 0.05;
        public Search search(String text, Site site,
                             PageRepository pageRepository, IndexRepository indexRepository,
                             FieldRepository fieldRepository, SiteRepository siteRepository) {
                this.indexRepository = indexRepository;
                this.fieldRepository = fieldRepository;
                this.pageRepository = pageRepository;

                SortedSet<SearchResult> searchResults = new TreeSet<>();

                if (site == null) {
                        siteRepository.findAll().forEach(s -> {
                                searchResults.addAll(getSearchResults(s, text));
                        });
                } else {
                        searchResults.addAll(getSearchResults(site, text));
                }

                Search search = new Search();

                search.setCount(searchResults.size());
                search.setResult(true);
                search.setData(searchResults);

                return search;
        }
        public static void removeLemmas (String text){
                ConcurrentHashMap<String, Lemma> lemmasMap = TransitionLink.getLemmasMap();
                for (String word : Lemmatizer.getLemmasList(text)) {
                        if (!(lemmasMap.get(word) == null) || !(lemmasMap.get(word).getPages().size() >
                                                PERCENTAGE_WORDS * TransitionLink.getAllLinks().size())) {
                                lemmaMap.put(word, lemmasMap.get(word));
                        }
                }
        }

        public static Map<String, Lemma> getSortedLemmas(String text) {
                removeLemmas(text);
                Map<String, Lemma> sortedMap = lemmaMap.entrySet()
                                .stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                        Map.Entry::getValue,
                                                        (e1, e2) -> e1,
                                                        LinkedHashMap::new));
                return sortedMap;
        }

        public static Set<Page> getEndPages(String text) {
                Map<String, Lemma> lemmaMap = SearchText.getSortedLemmas(text);
                HashSet<Page> pages = null;
                for (String word : lemmaMap.keySet()){
                        if (pages == null) {
                                pages = new HashSet<>(lemmaMap.get(word).getPages());
                        } else {
                               for (Page page : pages) {
                                       if (lemmaMap.get(word).getPages().contains(page)){
                                               pages = new HashSet<>(lemmaMap.get(word).getPages());
                                       }
                               }
                        }
                }
                return pages;
        }

        public static Map<Float, Page> getRelMap(String text){
                Map<Float, Page> map = new HashMap<>();
                float maxAbsRelevance = getAbsRelMap(getEndPages(text)).keySet().stream().toList().get(0);

                for (Map.Entry<Float, Page> entry : getAbsRelMap(getEndPages(text)).entrySet()){
                        map.put(entry.getKey() / maxAbsRelevance, entry.getValue());
                }
                return map.entrySet()
                        .stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new));
        }

        public static Map<Float, Page> getAbsRelMap (Set<Page> pages) {
                Map<Float, Page> absRel = new HashMap<>();
                for (Page page : pages) {
                        absRel.put(getAbsoluteRelevance(page), page);
                }
                return absRel.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new));
        }

        public static float getAbsoluteRelevance(Page page) {
                float absoluteRelevance = 0;
                for (Index index : page.getIndexList()){
                        absoluteRelevance += index.getLemmaRank();
                }
                return absoluteRelevance;
        }

        private SortedSet<SearchResult> getSearchResults(Site site, String text) {
                SortedSet<SearchResult> searchResults = new TreeSet<>();
                List<Field> fieldList = fieldRepository.findAll();

                for (Float relRel : getRelMap(text).keySet()){
                        Document document = Jsoup.parse(getRelMap(text).get(relRel).getContent());

                        AtomicReference<String> snippet = new AtomicReference<>("");
                        AtomicInteger maxSnippet = new AtomicInteger();
                        SearchResult searchResult = new SearchResult();
                        AtomicBoolean isHaven = new AtomicBoolean(false);

                        fieldList.forEach(field -> {

                                document.select(field.getSelector()).forEach(i -> {
                                        String str = i.text().toLowerCase();
                                        int count = 0;
                                        for (Lemma lem : lemmaMap.values()) {
                                                String l = lem.getLemma();
                                                if (str.contains(l)) {
                                                        count++;
                                                        str = str.replaceAll("(?i)" + l,
                                                                "<b>" + l + "</b>");
                                                } else {
                                                        lemmaMap.remove(lem);
                                                }
                                        }

                                        if (count > maxSnippet.get()) {
                                                snippet.set(str);
                                                maxSnippet.set(count);
                                                isHaven.set(true);
                                        }
                                });
                        });

                        if (isHaven.get()) {
                                searchResult.setTitle(document.title());
                                searchResult.setRelevance(relRel);
                                searchResult.setSnippet(snippet.get());
                                searchResult.setUri(getRelMap(text).get(relRel).getPath().replace(site.getUrl(), ""));
                                searchResult.setSite(site.getUrl());
                                searchResult.setSiteName(site.getName());

                                searchResults.add(searchResult);
                        }
                }
                return searchResults;
        }
}
