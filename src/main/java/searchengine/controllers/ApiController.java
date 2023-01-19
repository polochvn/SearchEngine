package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;
import searchengine.services.Storage;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;
    private final Storage storage;
    private final SiteRepository siteRepository;
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        boolean isStartIndexing = storage.startIndexing();
        JSONObject response = new JSONObject();
        try {
            if (isStartIndexing) {
                response.put("result", false);
                response.put("error", "Индексация уже запущена");
            } else {
                response.put("result", true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {
        boolean isStopIndexing = storage.stopIndexing();
        JSONObject response = new JSONObject();

        try {
            if (isStopIndexing) {
                response.put("result", false);
                response.put("error", "Индексация не запущена");
            } else {
                response.put("result", true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<String> indexPage(@RequestParam(name = "url") String url) {
        if (url == null) {
            return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }

        boolean isPageAdded = storage.indexPage(url);
        JSONObject response = new JSONObject();

        try {
            if (isPageAdded) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("error", "Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query", required = false) String query,
                                    @RequestParam(name = "site", required = false) String site,
                                    @RequestParam(name = "offset", defaultValue = "0") int offset,
                                    @RequestParam(name = "limit", defaultValue = "20") int limit) {
        if (query == null) {
            JSONObject response = new JSONObject();

            try {
                response.put("result", false);
                response.put("error", "Задан пустой поисковый запрос");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        }

        if (site == null) {
            return new ResponseEntity<>(storage.search(query, site, offset, limit), HttpStatus.OK);
        }

        if (siteRepository.findSiteByUrl(site).getUrl() == null) {
            JSONObject response = new JSONObject();

            try {
                response.put("result", false);
                response.put("error", "Указанная страница не найдена");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(response.toString(), HttpStatus.OK);
        }

        return new ResponseEntity<>(storage.search(query, site, offset, limit), HttpStatus.OK);
    }
}
