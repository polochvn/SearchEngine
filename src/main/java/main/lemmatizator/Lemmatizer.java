package main.lemmatizator;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Lemmatizer {

    private static LuceneMorphology luceneMorph;
    static {
        init();
    }
    public static void init(){
        if(luceneMorph == null) {
            try {
                luceneMorph = new RussianLuceneMorphology();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Integer> lemmatize(String text) {

        Map<String, Integer> wordMap = new HashMap<>();
            for (String str : getSeparateWordsList(text)) {

                List<String> wordBaseForms = luceneMorph.getNormalForms(str.toLowerCase(Locale.ROOT)
                                                        .replaceAll("\\p{Punct}", "")
                                                        .replaceAll("[^а-яё ]", "")
                                                        .replaceAll("ё", "е"));
                String word = wordBaseForms.get(0);
                String infoAboutWord = luceneMorph.getMorphInfo(word).get(0);

                if (!(infoAboutWord.contains("|n СОЮЗ") || infoAboutWord.contains("|l ПРЕДЛ") ||
                        infoAboutWord.contains("|f МС-П") || infoAboutWord.contains("|o МЕЖД") ||
                        infoAboutWord.contains("|e МС") || infoAboutWord.contains("|Z") ||
                        infoAboutWord.contains("|B"))) {
                    Integer oldCount = wordMap.get(word);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    wordMap.put(word, oldCount + 1);
                }
            }
            return wordMap;
    }
    public static List<String> getSeparateWordsList(String text) {
        String[] words = text.split("[\\W[\\d]&&[^а-яА-Я]]+");
        List<String> list = new ArrayList<>();
        for (String word : words) {
            if (word.matches("[\\d[a-zA-Z]]+") || text.equals("")) {
                continue;
            }
            list.add(word);
        }
        return list;
    }

    public static List<String> getLemmasList(String text) {
        return new ArrayList<>(Lemmatizer.lemmatize(text).keySet());
    }
}
