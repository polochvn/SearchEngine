package main.entities;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "Pages")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "path", nullable = false, unique = true, columnDefinition = "VARCHAR(256)")
    private String path;
    @Column(name = "code", nullable = false)
    private int code;
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToMany
    @JoinTable(name = "index",
                joinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    )
    private Set<Lemma> lemmas;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<main.entities.Index> indexList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<Lemma> getLemmaSet() {
        return lemmas;
    }

    public void setLemmaSet(Set<Lemma> lemmaSet) {
        this.lemmas = lemmaSet;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Set<Lemma> getLemmas() {
        return lemmas;
    }

    public void setLemmas(Set<Lemma> lemmas) {
        this.lemmas = lemmas;
    }

    public List<main.entities.Index> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<Index> indexList) {
        this.indexList = indexList;
    }
}
