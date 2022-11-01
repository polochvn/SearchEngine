package entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Lemmas")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

//    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "lemmas")
//    private Set<Page> pages;

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

//    public Set<Page> getPages() {
//        return pages;
//    }
//
//    public void setPages(Set<Page> pages) {
//        this.pages = pages;
//    }
}
