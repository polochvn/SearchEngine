package main.entities;
import javax.persistence.*;

@Entity
@Table(name = "Index")
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;
    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;
    @Column(name = "lemma_rank")
    private float lemmaRank;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public Lemma getLemma() {
        return lemma;
    }

    public void setLemma(Lemma lemma) {
        this.lemma = lemma;
    }

    public Float getLemmaRank() {
        return lemmaRank;
    }

    public void setLemmaRank(Float lemmaRank) {
        this.lemmaRank = lemmaRank;
    }
}
