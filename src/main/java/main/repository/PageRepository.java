package main.repository;

import main.entities.Page;
import main.entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Set<Page> findAllBySite(Site site);
    Integer countBySite(Site site);
}
