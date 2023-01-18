package searchengine.materializer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;

public class MorphInit {

    private static volatile LuceneMorphology luceneMorph;
    private MorphInit(){}
    static LuceneMorphology getLuceneMorph() throws IOException {
        if (luceneMorph == null) {
            synchronized (MorphInit.class) {
                if (luceneMorph == null){
                    luceneMorph = new RussianLuceneMorphology();
                }
            }
        }
        return luceneMorph;
    }
}
