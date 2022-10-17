import entities.Page;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Main {

    private static final String SITE_PATH = "http://www.playback.ru";

    public static void main(String[] args) {

        NodeLink node = new NodeLink(SITE_PATH);
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        TransitionLink searchingLinks = new TransitionLink(node, SITE_PATH);
        Set<Page> set = forkJoinPool.invoke(searchingLinks);

        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml").build();
            Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
            Session session = metadata.getSessionFactoryBuilder().build().openSession();
            Transaction transaction = session.beginTransaction();

            for (Page page : set) {
                session.saveOrUpdate(page);
            }
            transaction.commit();
            session.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
}