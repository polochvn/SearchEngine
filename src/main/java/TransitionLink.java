import entities.Page;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

public class TransitionLink extends RecursiveTask<Set<Page>> {

    private NodeLink nodeLink;
    private String sitePath;
    private static Set<String> allLinks = Collections.synchronizedSet(new LinkedHashSet<>());

    public TransitionLink(NodeLink nodeLink, String sitePath) {
        this.nodeLink = nodeLink;
        this.sitePath = sitePath;
    }

    @Override
    protected Set<Page> compute() {
        Set<Page> setPages = Collections.synchronizedSet(nodeLink.parseLink(nodeLink.getLink()));
        Set<NodeLink> nodeLinkSet = Collections.synchronizedSet(new CopyOnWriteArraySet<>());

        for (Page page : setPages) {
            if (!checkSet(page.getPath(), allLinks)) {
                NodeLink nodeLink = new NodeLink(sitePath + page.getPath());
                System.out.println(sitePath + page.getPath());
                nodeLinkSet.add(nodeLink);
                allLinks.add(page.getPath());
            }
        }
        nodeLink.setNodeLinkSet(nodeLinkSet);

        List<TransitionLink> listTask = new ArrayList<>();
        for (NodeLink node : nodeLink.getNodeLinkSet()) {
            TransitionLink task = new TransitionLink(node, sitePath);
            task.fork();
            listTask.add(task);
        }
        addResultsFromTasks(setPages, listTask);
        return setPages;
    }

    private void addResultsFromTasks(Set<Page> set, List<TransitionLink> tasks) {
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