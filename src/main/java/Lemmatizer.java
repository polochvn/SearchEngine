import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Lemmatizer {

    public static void lemmatize(String text) {

        String[] words = text.split("[\\s,.]+");
        Map<String, Integer> wordMap = new HashMap<>();
            for (String str : words) {
                LuceneMorphology luceneMorph = null;
                try {
                    luceneMorph = new RussianLuceneMorphology();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                List<String> wordBaseForms = luceneMorph.getNormalForms(str.toLowerCase(Locale.ROOT));
                String word = wordBaseForms.get(0);
                String infoAboutWord = luceneMorph.getMorphInfo(word).get(0);

                if (!(infoAboutWord.contains("СОЮЗ") || infoAboutWord.contains("МЕЖД") ||
                        infoAboutWord.contains("ПРЕДЛ") || infoAboutWord.contains("ЧАСТ"))) {
                    Integer oldCount = wordMap.get(word);
                    if (oldCount == null) {
                        oldCount = 0;
                    }
                    wordMap.put(word, oldCount + 1);
                }
            }
        for (String word : wordMap.keySet()) {
            System.out.println(word + " - " + wordMap.get(word));
        }
    }
}
