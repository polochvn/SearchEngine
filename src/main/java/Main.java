import entities.Lemma;
import entities.Page;
import jakarta.persistence.criteria.CriteriaDelete;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Main {

    private static final String SITE_PATH = "http://www.playback.ru";

    public static void main(String[] args) {
        String[] str = new String[]{"Интернет-магазин Although-you specify any initial pool size," +
                "the pool adjusts its size dynamically to maintain enough " +
                "active threads at any given time. dynamically Another 87554 significant difference compared " +
                "to other ExecutorService's is that this pool dynamically need not be explicitly " +
                "shutdown upon program exit because all that its that threads are in daemon mode.",
                "Интернет-магазин Although-you specify any initial pool size," +
                        "the pool adjusts its size dynamically to maintain enough " +
                        "active threads at any given time. dynamically Another 87554 significant difference compared " +
                        "to other ExecutorService's is that this pool dynamically need not be explicitly " +
                        "shutdown upon program exit because all that its that threads are in daemon mode."};

        try {
        NodeLink node = new NodeLink(SITE_PATH);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        TransitionLink searchingLinks = new TransitionLink(node, SITE_PATH);
        Set<String> set = forkJoinPool.invoke(searchingLinks);


            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml").build();
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            Session session = metadata.getSessionFactoryBuilder().build().openSession();
            Transaction transaction = session.beginTransaction();

            HashMap<String, Lemma> lemmaMap = new HashMap<>();
            for (String link : set) {
                Page page = new Page();
                page.setPath(link);
                page.setContent(Jsoup.connect(SITE_PATH + link).get().html());
                Connection.Response response = Jsoup.connect(SITE_PATH + link)
                                    .get().connection().response();
                int code = response.statusCode();
                page.setCode(code);
                session.saveOrUpdate(page);

                Document doc = Jsoup.parse(page.getContent());

//                Field title = new Field();
//                title.setName(doc.tagName("title").tagName());
//                title.setSelector(doc.tagName("title").cssSelector());
//                title.setWeight(1F);
//                session.saveOrUpdate(title);

                for (String word : Lemmatizer.lemmatize(doc.select("title, body").text()).keySet()) {
                    Lemma oldLemma = lemmaMap.get(word);
                    if (oldLemma == null){
                        oldLemma = new Lemma();
                        oldLemma.setLemma(word);
                        oldLemma.setFrequency(1);
                    } else {
                        Integer freq = oldLemma.getFrequency();
                        oldLemma.setFrequency(freq + 1);
                    }
                    lemmaMap.put(word, oldLemma);
                }

//                Field body = new Field();
//                body.setName(doc.tagName("body").tagName());
//                body.setSelector(doc.tagName("body").cssSelector());
//                body.setWeight((float) 0.8);
//                session.saveOrUpdate(body);
            }

            for (String str7 : lemmaMap.keySet()){
                session.saveOrUpdate(lemmaMap.get(str));
            }

            transaction.commit();
            session.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}