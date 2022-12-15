package main.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Lemmas")
public class Lemma implements Comparable<Lemma>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lemma", nullable = false, insertable = false, updatable = false)
    private String lemma;

    @Column(name = "lemma", nullable = false)
    private Integer frequency;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "lemmas")
    private Set<Page> pages;

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
        return this.frequency.compareTo(o.frequency); }
}
