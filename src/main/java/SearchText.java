import entities.Lemma;
import entities.Page;
import lemmatizator.Lemmatizer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SearchText {

        private static Map<String, Lemma> lemmata = new HashMap<>();

        private static final Double PERCENTAGE_WORDS = 0.05;

        public static void removeLemmas (String text){
                ConcurrentHashMap<String, Lemma> lemmasMap = TransitionLink.getLemmasMap();
                for (String word : Lemmatizer.getLemmasList(text)) {
                        if (!(lemmasMap.get(word) == null) || !(lemmasMap.get(word).getPages().size() >
                                                PERCENTAGE_WORDS * TransitionLink.getAllLinks().size())) {
                                lemmata.put(word, lemmasMap.get(word));
                        }
                }
        }

        public static Map<String, Lemma> getSortedLemmas() {
                Map<String, Lemma> sortedMap = lemmata.entrySet()
                                .stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                                        Map.Entry::getValue,
                                                        (e1, e2) -> e1,
                                                        LinkedHashMap::new));
                return sortedMap;
        }

        public static Set<Page> getEndPages() {
                Map<String, Lemma> lemmaMap = SearchText.getSortedLemmas();
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

        public static Map<Float, Page> getRelMap(){
                Map<Float, Page> map = new HashMap<>();
                float maxAbsRelevance = getAbsRelMap(getEndPages()).keySet().stream().toList().get(0);

                for (Map.Entry<Float, Page> entry : getAbsRelMap(getEndPages()).entrySet()){
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
                for (Lemma lemma : page.getLemmaSet()){
                        absoluteRelevance += TransitionLink.getIndices().get(lemma.getLemma()).getRank();
                }
                return 0;
        }
}
