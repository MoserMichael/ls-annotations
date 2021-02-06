package lsann.graph;

import lsann.ClassHierarchyAsmClassVisitor;

import java.util.Collections;
import java.util.stream.Collectors;

public class HierarchyGraphVisitors {
    public static class ShowHierarchyVisitor<EntryData> implements HierarchyGraph.HierarchyGraphVisitor<EntryData> {

        @Override
        public void visit(String entryName, EntryData entryData, int nestingLevel) {
            //System.out.printf("%s%s\n", nestingPrefix(nestingLevel), entryName);

            if (entryData != null) {
                ClassHierarchyAsmClassVisitor.ClassEntryData centry = (ClassHierarchyAsmClassVisitor.ClassEntryData) entryData;
                System.out.printf("%s%s %s\n", nestingPrefix(nestingLevel), entryName, entryData.toString());
            } else {
                System.out.printf("%s%s\n", nestingPrefix(nestingLevel), entryName);
            }
        }

        @Override
        public void nodeNotFound(String entryName, int nestingLevel) {
            System.out.printf("Error: %s class not found. nesting %d", entryName, nestingLevel);
        }

        private String nestingPrefix(int nesting) {
            return Collections.nCopies(nesting, "    ").stream().collect(Collectors.joining());
        }
    }
}
