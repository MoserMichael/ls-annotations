package lsann.graph;
import java.util.*;
import java.util.stream.Collectors;


public class HierarchyGraph<EntryData, LinkData> {

    public static interface HierarchyGraphVisitor<EntryData> {
        public void visit(String entryName, EntryData entry, int nestingLevel);
        public void nodeNotFound(String entryName, int nestingLevel);
    }


    private static class LinkEntry<LinkData> {
        LinkEntry(String linkKey, LinkData data) {
            this.linkToKey = linkKey;
            this.linkData = data;
        }

        String linkToKey;
        LinkData linkData;
    }

    public static class Entry<EntryData, LinkData> {
        List<LinkEntry> outgoingLinkList;
        List<LinkEntry> incomingLinkList;
        EntryData entryData;

        public Entry(EntryData data) {
            this.outgoingLinkList = new LinkedList<LinkEntry>();
            this.incomingLinkList = new LinkedList<LinkEntry>();
            this.entryData = data;
        }

        void addLinkEntry(String linkToKey, LinkData linkData, boolean addOutgoingLink) {
            List<LinkEntry> list;
            if (addOutgoingLink) {
                list = this.outgoingLinkList;
            } else {
                list = this.incomingLinkList;
            }
            if (!list.contains(linkToKey)) {
                list.add(new LinkEntry(linkToKey, linkData) );
            }
        }
    }
    private Map<String,Entry> mapKeyToNodeToEntry;

    public HierarchyGraph() {
        mapKeyToNodeToEntry = new HashMap<String,Entry>();
    }

    public Entry addNode(String nodeKey, EntryData nodeData) {
        Entry val = mapKeyToNodeToEntry.get( nodeKey );
        if (val == null) {
            val = new Entry(nodeData);
            mapKeyToNodeToEntry.put(nodeKey, val);
        }
        if (nodeData != null){
            val.entryData = nodeData;
        }
        return val;
    }

    public void addTwoWayLink(String fromNodeKey, String toNodeKey, LinkData linkData) {
        Entry baseEntry = addNode(fromNodeKey,null);
        Entry derivedEntry = addNode(toNodeKey, null);
        baseEntry.addLinkEntry(toNodeKey, linkData, true);
        derivedEntry.addLinkEntry(fromNodeKey,  linkData, false);
    }

    public Entry findNode(String nodeKey) {
        return mapKeyToNodeToEntry.get(nodeKey);
    }

    public void walkDerived(HierarchyGraphVisitor visitor, String nodeKey) {
        walkDerivedOrBase(visitor, 0, nodeKey, true);
    }

    public void walkBases(HierarchyGraphVisitor visitor, String nodeKey) {
        walkDerivedOrBase(visitor, 0, nodeKey, false);
    }

    private void walkDerivedOrBase(HierarchyGraphVisitor visitor, int nestingLevel, String nodeKey, boolean walkDirection) {
        HierarchyGraph.Entry entry;

        entry = findNode(nodeKey);
        if (entry == null) {
            visitor.nodeNotFound(nodeKey, nestingLevel);
            //System.out.printf("%sError: %s class not found", nestingPrefix(nestingLevel), entryName);
            return;
        }

        visitor.visit(nodeKey, entry.entryData, nestingLevel);

        List<LinkEntry> entryList = null;
        if (walkDirection) {
            entryList = entry.outgoingLinkList;
        } else {
            entryList = entry.incomingLinkList;
        }

        List<String> sortedNames =  entryList.stream().map(e -> e.linkToKey).sorted().collect(Collectors.toList());

        for(String chEntry : sortedNames) {
            walkDerivedOrBase( visitor,nestingLevel+1, chEntry, walkDirection);
        }
    }

}
