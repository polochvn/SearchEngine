import entities.Field;
import lemmatizator.Lemmatizer;
import org.jsoup.Jsoup;

import java.util.*;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String SITE_PATH = "http://www.playback.ru";
    private static final Map<String, Float> TAGS = Map.of("title", 1F, "body", 0.8F);

    public static void main(String[] args) {
        String[] str = new String[]{"Интернет-магазин Although-you specify any initial pool size," +
                "the pool adjusts its size dynamically to maintain enough " +
                "active threads at any given time. dynamically Another 87554 significant difference compared " +
                "to other ExecutorService's is that this pool dynamically need not be explicitly " +
                "shutdown upon program exit because all that its that threads are in daemon mode.",
                "Интернет-магазин Although-you specify any initial pool size," + "the pool adjusts its size dynamically to maintain enough " +
                        "active threads at any given time. dynamically Another 87554 significant difference compared " +
                        "to other ExecutorService's is that this pool dynamically need not be explicitly " +
                        "shutdown upon program exit because all that its that threads are in daemon mode."};

        for (String string : Lemmatizer.lemmatize("").keySet()){
            System.out.println(string);
        }

        try {
        NodeLink node = new NodeLink(SITE_PATH);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        TransitionLink searchingLinks = new TransitionLink(node, SITE_PATH);
        forkJoinPool.invoke(searchingLinks);

            for (String str7 : TransitionLink.getLemmasMap().keySet()){
                InitialSession.getSession().saveOrUpdate(TransitionLink.getLemmasMap().get(str7));
            }

            for (String keyIndex : TransitionLink.getIndices().keySet()) {
                InitialSession.getSession().saveOrUpdate(TransitionLink.getIndices().get(keyIndex));
            }

            for (String tag : TAGS.keySet()){
                Field field = new Field();
                field.setName(Jsoup.parse(SITE_PATH).tagName(tag).tagName());
                field.setSelector(Jsoup.parse(SITE_PATH).cssSelector());
                field.setWeight(TAGS.get(tag));
                InitialSession.getSession().saveOrUpdate(field);
            }
            InitialSession.closeSession();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}