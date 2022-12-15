package main.entities;

import javax.persistence.*;

@Entity
@Table(name = "Fields")
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "selector", nullable = false)
    private String selector;

    @Column(name = "weight", nullable = false)
    private float weight;

    public void setName(String name) {
        this.name = name;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSelector() {
        return selector;
    }

    public float getWeight() {
        return weight;
    }
}
