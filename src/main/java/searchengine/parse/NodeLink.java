package searchengine.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
public class NodeLink {

    private final String link;
    private Document document;
    public NodeLink(String link) {

        this.document = null;
        this.link = link;
    }
    public NodeLink(String link, Document document) {

        this.document = document;
        this.link = link;
    }

    public String parseOneLink(String link){

            if (document == null) {
                try {
                    document = Jsoup.connect(link).get();
                    Elements links = document.select("a[href]");
                    for (Element linkSite : links) {

                        String relLink = linkSite.attr("href");
                        if (link.contains(relLink) && relLink.length() > 1) {
                            return relLink;
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        return "";
    }

    public Set<String> parseLink (String link) {

        Set<String> setLinks = new CopyOnWriteArraySet<>();
        try {
            if (document == null) {
                document = Jsoup.connect(link).get();
            }
            Elements links = document.select("a[href]");
            for (Element linkSite : links) {

                String relLink = linkSite.attr("href");
                if (!relLink.contains("http") && !(relLink.matches(".*\\.(bmp|gif|jpg|png|js|css|#)$"))
                            && relLink.startsWith("/") && relLink.length() > 1){
                        setLinks.add(relLink);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setLinks;
    }
    public String getLink() {
        return link;
    }
}
