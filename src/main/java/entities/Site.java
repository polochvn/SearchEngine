package entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "text")
    private String error;

    @Enumerated(EnumType.ORDINAL)
    private Status status;
    @Column(nullable = false)
    private String url;
    @Column(nullable = false)
    private String name;
    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY
    )
    private Set<Page> pages = new HashSet<>();

    @OneToMany(mappedBy = "site",
            fetch = FetchType.LAZY
    )
    private Set<Lemma> lemmas = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public LocalDateTime getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Page> getPages() {
        return pages;
    }

    public void setPages(Set<Page> pages) {
        this.pages = pages;
    }

    public Set<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(Set<Lemma> lemmas) {
        this.lemmas = lemmas;
    }
}
