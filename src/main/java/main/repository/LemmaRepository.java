package main.repository;

import main.entities.Lemma;
import main.entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaByLemmaAndSite(String lemma, Site site);
    Integer countBySite(Site site);
}
