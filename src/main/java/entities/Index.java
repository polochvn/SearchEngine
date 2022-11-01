package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Index")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private Integer page_id;

    @Column(nullable = false)
    private Integer lemma_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private Page page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", nullable = false)
    private Lemma lemma;

    @Column(nullable = false)
    private Float rank;

    public Integer getId() {
        return id;
    }

    public Integer getPage_id() {
        return page_id;
    }

    public Integer getLemma_id() {
        return lemma_id;
    }

    public Float getRank() {
        return rank;
    }
}
