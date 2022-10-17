package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Indexes")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private Integer page_id;

    @Column(nullable = false)
    private Integer lemma_id;

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
