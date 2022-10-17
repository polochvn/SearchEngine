package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Lemmas")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    public Integer getId() {
        return id;
    }

    public String getLemma() {
        return lemma;
    }

    public Integer getFrequency() {
        return frequency;
    }
}
