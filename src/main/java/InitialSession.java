import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class InitialSession {

    private static volatile Session session;
    private static volatile Transaction transaction;

    private InitialSession() {
    }

    public static Session getSession() {
        if (session == null) {
            synchronized (Session.class) {
                StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                        .configure("hibernate.cfg.xml").build();
                Metadata metadata = new MetadataSources(registry).getMetadataBuilder().build();
                session = metadata.getSessionFactoryBuilder().build().openSession();
                transaction = session.beginTransaction();
            }
        }
        return session;
    }
    public static void closeSession(){
        session.close();
        transaction.commit();
    }
}
