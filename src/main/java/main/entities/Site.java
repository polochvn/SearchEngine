package main.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "site")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "status_time", columnDefinition = "DATETIME", nullable = false)
    private Date statusTime;
    @Column(name = "error")
    private String error;

    @Enumerated(EnumType.ORDINAL)
    private Status status;
    @Column(name = "url", nullable = false)
    private String url;
    @Column(name = "name", nullable = false)
    private String name;
    private Integer pages;
    private Integer lemmas;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
    }

    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages (Integer pages) {
        this.pages = pages;
    }

    public Integer getLemmas() {
        return lemmas;
    }

    public void setLemmas(Integer lemmas) {
        this.lemmas = lemmas;
    }
}
