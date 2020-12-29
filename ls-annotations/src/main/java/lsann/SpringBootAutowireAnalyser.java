package lsann;

import lsann.graph.HierarchyGraph;

import java.util.*;


public class SpringBootAutowireAnalyser {
    private HierarchyGraph typeHierarchy;
    private Map<String, AstDefinition.ClassRep> tinfo;

    SpringBootAutowireAnalyser(HierarchyGraph th, Map<String, AstDefinition.ClassRep> tinfo) {
        this.typeHierarchy = th;
        this.tinfo = tinfo;
    }

    //entry points
    //find all @SpringBootApplication
    //find those with @ComponentScan
    //  find which classes fall under component scan for this applications.

    //find all Component types (annotation definitions with Component annotation)

    //find all classes that fall under component scan with any of the component typw annotations
    //  - the constructors are @autowired.

    // find all the arguments with @autowired annotations (
    void scan() {
    }
    /*
        // check if ant any class is annotated with org.springframework.boot.autoconfigure.SpringBootApplication
        // ... and if annotation org.springframework.context.annotation.ComponentScan supported:
        List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>> findSBootApp = findClassesWithAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication");
        if (findSBootApp.isEmpty()) {
            System.out.println("Error: no class with attribute ComponentScan detected");
            return;
        }

        System.out.println("Found classe with SprintBootApplication annotation!");

        //"org.springframework.context.annotation.ComponentScan");
        List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>> listCompScan = findClassesWithAnnotation(findSBootApp, "org.springframework.context.annotation.ComponentScan");
        if (listCompScan.isEmpty()) {
            return;
        }

        System.out.println("Classes with both SpringBootApplicationa and ComponentScan:\n" + listCompScan.stream().map( e -> e.getValue().type + "\n").collect(Collectors.joining()));

        // search for @Autowire annotation in possible locations. (also @Components with constructors)

        // search for all annotations with @Bean (or equivalent) annotations.

        // resolve @Autowire references for types allowed in comonentscan.

    }

    private boolean hasAnnotation(AnnotationGraphClassVisitor.ClassRep classRep, String annoType) {
        for (AnnotationGraphClassVisitor.AnnotationRep rep : classRep.getAnnotations()) {
            if (rep.type.equals(annoType)) { // == "org.springframework.context.annotation.ComponentScan") {
                return true;
            }
        }
        return false;
    }

    private List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>> findClassesWithAnnotation(String typeName, List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>> appEntry) {
        return appEntry.stream().filter(e -> hasAnnotation(e.getValue(), typeName)).collect(Collectors.toList());
    }

    private List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>> filterClassWithAnnotation(String typeName, List<Map.Entry<String, AnnotationGraphClassVisitor.ClassRep>>  tinfo)
    {
        return tinfo.stream().filter(e ->hasAnnotation(e.getValue(), typeName)).collect(Collectors.toList());
    }
    */

}
