package repository;

import entities.Page;
import entities.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findPath(String path);
    Set<Page> pagesOnSite(Site site);
    Page findByPath(String path);
}
