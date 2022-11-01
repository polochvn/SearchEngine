import entities.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public class NodeLink {

    private final String link;
    private Set<NodeLink> nodeLinkSet;
    private static final String SITE_PATH = "http://www.playback.ru/";

    public NodeLink(String link) {
        this.link = link;
    }

    public Set<String> parseLink (String link) {

        Document document = null;
        Set<String> setLinks = new CopyOnWriteArraySet<>();
        try {
            document = Jsoup.connect(link).get();
            Thread.sleep(100);

        if (!(document == null)) {

            Elements links = document.select("a[href]");
            for (Element linkSite : links) {

                String absLink = linkSite.attr("abs:href");
                String relLink = linkSite.attr("href");
                if (absLink.contains(SITE_PATH) &&
                        !(relLink.matches(".*\\.(bmp|gif|jpg|png|js|css|#)$"))){
                    setLinks.add(relLink);
                }
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

    public void setNodeLinkSet(Set<NodeLink> nodeLinkSet) {
        this.nodeLinkSet = nodeLinkSet;
    }
}
