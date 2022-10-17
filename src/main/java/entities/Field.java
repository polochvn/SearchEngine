package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Fields")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", insertable = false, updatable = false, nullable = false)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String selector;

    @Column(nullable = false)
    private Float weight;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSelector() {
        return selector;
    }

    public Float getWeight() {
        return weight;
    }
}
