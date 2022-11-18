package entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Lemmas")
public class Lemma implements Comparable<Lemma>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_lemma_site"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site site;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "lemmas")
    private Set<Page> pages;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Index> indexList;

    public Lemma() {
        pages = new HashSet<>();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getId() {
        return id;
    }

    public String getLemma() {
        return lemma;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public Set<Page> getPages() {
        return pages;
    }

    public void setPages(Set<Page> pages) {
        this.pages = pages;
    }
    @Override
    public int compareTo(Lemma o) {
        return this.frequency.compareTo(o.frequency);
    }
}
