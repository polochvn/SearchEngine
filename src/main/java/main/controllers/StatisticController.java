package main.controllers;


import main.Source;
import main.entities.ResStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class StatisticController {

    @Autowired
    private Source source;

    @GetMapping("/statistics")
    public ResponseEntity<ResStatistics> getStatistics() {
        return ResponseEntity.ok().body(source.getStatistic());
    }
}