package searchengine.lemmatizator;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.repository.LemmaRepository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Materialize {
    private static LemmaRepository lemmaRepository;
    public Lemma getLemma(String word, Site site) {

        if (checkRussianForm(word)) {
            return lemmaRepository.findLemmaByLemmaAndSite(word, site);
        }
        return null;
    }

    private boolean checkRussianForm(String word) {

        String russianAlphabet = "[а-яА-Я]+";

        if (!word.matches(russianAlphabet)) {
            return false;
        }
        List<String> wordBaseForm;
        try {
            wordBaseForm = MorphInit.getLuceneMorph().getMorphInfo(word);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return checkWrongType(wordBaseForm.toString());
    }

    private static boolean checkWrongType(String word) {
        return !word.contains("|n СОЮЗ") && !word.contains("|l ПРЕДЛ") &&
                !word.contains("|f МС-П") && !word.contains("|o МЕЖД") &&
                !word.contains("|e МС") && !word.contains("|Z") &&
                !word.contains("|B") && !word.contains("ВВОДН") &&
                !word.contains("ЧАСТ");
    }

    public static Map<String, Integer> legitimatize(String text){

        Map<String, Integer> wordMap = new HashMap<>();
            for (String str : getSeparateWordsList(text)) {

                //System.out.println(str.toLowerCase(Locale.ROOT));
                String word = null;
                try {
                    word = MorphInit.getLuceneMorph().getNormalForms(str.toLowerCase(Locale.ROOT)
                                                            .replaceAll("\\p{Punct}", "")
                                                            .replaceAll("[^а-яё ]", "")
                                                            .replaceAll("ё", "е")).get(0);
                    String infoAboutWord = MorphInit.getLuceneMorph().getMorphInfo(word).get(0);

                    if (checkWrongType(infoAboutWord)) {
                        Integer oldCount = wordMap.get(word);
                        if (oldCount == null) {
                            oldCount = 0;
                        }
                        wordMap.put(word, oldCount + 1);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return wordMap;
    }
    public static List<String> getSeparateWordsList(String text) {
        String[] words = text.split("[\\W\\d&&[^а-яА-Я]]+");
        List<String> list = new ArrayList<>();
        for (String word : words) {
            if (word.matches("[\\d[a-zA-Z_]]+") || text.equals("")) {
                continue;
            }
            list.add(word);
        }
        return list;
    }

    public static void setLemmaRepository(LemmaRepository lemmaRepository) {
        Materialize.lemmaRepository = lemmaRepository;
    }
}
