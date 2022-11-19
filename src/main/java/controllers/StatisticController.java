package controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class StatisticController {

    @GetMapping("/statistics")
    public ResponseEntity<Object> getStatistics() {
        return ResponseEntity.ok().body(new Object());
    }
}
