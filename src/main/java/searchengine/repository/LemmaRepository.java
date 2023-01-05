package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaByLemmaAndSite(String lemma, Site site);
    Integer countBySite(Site site);
}
