package repository;

import entities.Lemma;
import entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    Lemma findLemmaByLemmaAndSite(String lemma, Site site);
    Set<Lemma> lemmasOnSite(Site site);
}
