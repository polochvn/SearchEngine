package searchengine.parse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import searchengine.config.Status;
import searchengine.model.Site;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;

@RequiredArgsConstructor
@Getter
@Setter
public class PoolThread extends Thread {
    private final TransitionLink transition;
    private final List<ForkJoinPool> forkJoinPools;
    private final SiteRepository siteRepository;
    private ForkJoinPool pool;
    private final int numberThreads;

    @Override
    public void run() {
        Site site = transition.getSite();

        try {
           pool = new ForkJoinPool(numberThreads);
           forkJoinPools.add(pool);
           pool.execute(transition);
           transition.join();

           if (!pool.isTerminating()) {
               saveStoppedSite(site);
           }

           site.setStatus(Status.INDEXED);
           siteRepository.save(site);

        } catch (RejectedExecutionException ex) {
            ex.printStackTrace();
            saveStoppedSite(site);
        }
    }
    public void saveStoppedSite(Site site) {
        site.setError("Индексация остановлена принудительно!");
        site.setStatus(Status.FAILED);
        siteRepository.save(site);
    }
}
