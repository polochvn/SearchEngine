package controllers;

@Controller
public class DefaultController {
    @RequestMapping("/admin")
    public String index() {
// ищет файл index.html в resources/templates
        return "index";
    }
}
