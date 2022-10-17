import entities.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;

public class NodeLink {

    private final String link;
    private Set<NodeLink> nodeLinkSet;
    private static Set<Page> setPages;

    public NodeLink(String link) {
        this.link = link;
        setPages = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    public Set<Page> parseLink (String link) {

        Document document = null;
        try {
            document = Jsoup.connect(link).get();
            Thread.sleep(100);

        if (!(document == null)) {

            Elements links = document.select("a[href]");
            for (Element linkSite : links) {
                String absLink = linkSite.attr("abs:href");
                String relLink = linkSite.attr("href");
                if (absLink.startsWith(link) || !(relLink.matches(".*\\.(bmp|gif|jpg|png|js|css|#)$"))){
                    Page page = new Page();
                    page.setPath(relLink);
                    page.setContent(Jsoup.connect(absLink).get().html());
                    Connection.Response response = document.connection().response();
                    int code = response.statusCode();
                    page.setCode(code);
                    setPages.add(page);
                }
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return setPages;
    }

    public String getLink() {
        return link;
    }

    public Set<NodeLink> getNodeLinkSet() {
        return nodeLinkSet;
    }

    public void setNodeLinkSet(Set<NodeLink> nodeLinkSet) {
        this.nodeLinkSet = nodeLinkSet;
    }

    public static Set<Page> getSetPages() {
        return setPages;
    }
}
