import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Lemmatizer {

    public static Map<String, Integer> lemmatize(String text) {

        String[] words = text.split("[\\W[\\d]&&[^а-яА-Я]]+");
        Map<String, Integer> wordMap = new HashMap<>();
            for (String str : words) {
                if (str.matches("[\\d]+")) {
                    continue;
                }
                LuceneMorphology luceneMorph = null;
                try {
                    luceneMorph = (str.matches("[a-zA-Z]+")) ? new EnglishLuceneMorphology() :
                                                                    new RussianLuceneMorphology();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> wordBaseForms = luceneMorph.getNormalForms(str.toLowerCase(Locale.ROOT));
                String word = wordBaseForms.get(0);
                String infoAboutWord = luceneMorph.getMorphInfo(word).get(0);

                if (!(infoAboutWord.contains("СОЮЗ") || infoAboutWord.contains("МЕЖД") ||
                        infoAboutWord.contains("ПРЕДЛ") || infoAboutWord.contains("ЧАСТ") ||
                        infoAboutWord.matches("\\d"))) {
                    Integer oldCount = wordMap.get(word);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    wordMap.put(word, oldCount + 1);
                    System.out.println(word);
                }
            }
            return wordMap;
    }
}
