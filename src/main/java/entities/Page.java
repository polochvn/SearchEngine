package entities;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Pages")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(nullable = false)
    private Integer code;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

//    @ManyToMany
//    @JoinTable(name = "index",
//                joinColumns = @JoinColumn(name = "page_id", referencedColumnName = "id"),
//                inverseJoinColumns = @JoinColumn(name = "lemma_id", referencedColumnName = "id")
//    )
//    private Set<Lemma> lemmas;

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

//    public Set<Lemma> getLemmaSet() {
//        return lemmas;
//    }
//
//    public void setLemmaSet(Set<Lemma> lemmaSet) {
//        this.lemmas = lemmaSet;
//    }
}
