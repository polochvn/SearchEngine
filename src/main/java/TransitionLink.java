import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class TransitionLink extends RecursiveTask<Set<String>> {

    private NodeLink nodeLink;
    private String sitePath;
    private static Set<String> allLinks = new CopyOnWriteArraySet<>();

    public TransitionLink(NodeLink nodeLink, String sitePath) {
        this.nodeLink = nodeLink;
        this.sitePath = sitePath;
    }

    @Override
    protected Set<String> compute() {
        Set<String> links = Collections.synchronizedSet(nodeLink.parseLink(nodeLink.getLink()));
        Set<NodeLink> nodeLinkSet = new CopyOnWriteArraySet<>();

        for (String page : links) {
            if (!checkSet(page, allLinks)) {
                NodeLink nodeLink = new NodeLink(sitePath + page);
                //System.out.println(sitePath + page);
                nodeLinkSet.add(nodeLink);
                allLinks.add(page);
                System.out.println(allLinks.size());
            }
        }
        nodeLink.setNodeLinkSet(nodeLinkSet);

        List<TransitionLink> listTask = new ArrayList<>();
        for (NodeLink node : nodeLinkSet) {
            TransitionLink task = new TransitionLink(node, sitePath);
            task.fork();
            listTask.add(task);
        }
        addResultsFromTasks(links, listTask);
        return links;
    }

    private void addResultsFromTasks(Set<String> set, List<TransitionLink> tasks) {
            for (TransitionLink item : tasks) {
                set.addAll(item.join());
            }
    }

    private boolean checkSet(String str, Set<String> set) {
        if (!(set == null)) {
            for (String link : set) {
                if (link.equals(str)) {
                    return true;
                }
            }
        }
        return false;
    }
}