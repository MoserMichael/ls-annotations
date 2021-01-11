## ls-annotation

ls-annotation is a java bytecode decompiler; it extracts and prints definitions of classes and methods that use java annotations.

The purpose of this tool is to make sense out of declarations with annotations that depend on one another, an example for this kind of dependencies is @Autowire from Spring Boot:
In Spring Boot a function that is marked with @Autowire will get the value returned by a function marked with the @Bean annotation, provided that this function
returns the same type or a derived class as that of the parameter type marked with @Autowire, and that the class with the Bean annotation is included in the @ComponentScan annotation.
The same mechanism is applied to constructor arguments of classe marked with the @Component annotation.

Now you can have some of the features of following up on annotations in IntelliJ, but this program is nice if you prefer to look at text.

The motivation for this tool is to extract all classes/methods that have an annotation - and do so based on the bytecode of a system. I think that searching through the bytecode is very effective way to look at complex java based systems; Searching through the source code is much more limited, often one is faced with the problem of chasing multiple dependencies and their versions (and one might have to do so across multiple source code repositories); one doesn't have this problem when looking at the complete binary byte code representation of a given system.

Limitations: this program can detect detect annotations placed into the byte code, it can detect annotations with retention policy CLASS and RUNGIME [link](https://docs.oracle.com/javase/7/docs/api/java/lang/annotation/RetentionPolicy.html). It can't detect annotations with retention policy SOURCE that are not put into bytecode, for example @Override is one of these.

## Installation 

Download ls-annotation.jar from the [release page](https://github.com/MoserMichael/ls-annotations/releases/)

you need JDK version 8 to run this program.

## Usage

ls-annotation is a command line utility;


## Command line options
<pre>
java -jar ls-annotations [[-l|--list] className|[-b|--baseOf] className]|[[-d|--derivedOf]] [--scanRec|-r] [&lt;directory&gt;|&lt;jar file&gt;]*

Display all definitions with annotations
it works by scans by decompiling the bytecode of class files selectively to shows all definitions with annotations.
helps to decipher systems with interdependent annotations (like spring/grpc, etc)

Common Arguments:
 &lt;directory&gt;       scans directory recursively for jarsa nd object files to scan
 &lt;jar file&gt;        scans jar file
 --scanRec -r      scans jars contained in jars (default off) (optional)
 -v                (debug) very verbose, trace objectweb events

Commands:
 --l               List all annotated classes or methods and show the annotations
 --list
 -d &lt;n&gt;            List all derived classes or interfaces of class/interface &lt;n&gt;
 --derivedClassOf &lt;n&gt;

 -b &lt;n&gt;            List all base classes or interfaces of class/interface &lt;n&gt;
 --baseClassOf &lt;n&gt;

 -a &lt;n&gt;            List all derived annotations of annotation &lt;n&gt;
 --derivedAnnoOf &lt;n&gt;
 -e &lt;n&gt;            List all base annotations of annotation &lt;n&gt;
 --baseAnnoOf &lt;n&gt;

 -u &lt;n&gt;            Show all uses of annotation &lt;n&gt;
 --annoUsage &lt;n&gt;

 -w &lt;n&gt;            Show really all uses of annotation &lt;n&gt;
 --annoRec &lt;n&gt;     Including annotations that extend the given one and their usage



</pre>

## Example usage

    
<hr>
<details>
<summary>show all annotations in jar file</summary>

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/libs/ls-annotations.jar</code>

Output:
<pre>
    File: ./ls-annotations/build/libs/ls-annotations.jar - org/objectweb/asm/ClassReader.class
    
    public class org.objectweb.asm.ClassReader{
    
        @java.lang.Deprecated
        public final deprecated byte[] b;}
    
    File: ./ls-annotations/build/libs/ls-annotations.jar - org/objectweb/asm/ClassWriter.class
    
    public class org.objectweb.asm.ClassWriter
      extends org.objectweb.asm.ClassVisitor{
    
        @java.lang.Deprecated
        public deprecated int newHandle(int,java.lang.String,java.lang.String,java.lang.String);
    }
    
    File: ./ls-annotations/build/libs/ls-annotations.jar - org/objectweb/asm/Handle.class
    
    public final class org.objectweb.asm.Handle{
    
        @java.lang.Deprecated
        public deprecated &lt;init&gt;(int,java.lang.String,java.lang.String,java.lang.String);
    }
    
    File: ./ls-annotations/build/libs/ls-annotations.jar - org/objectweb/asm/MethodVisitor.class
    
    public abstract class org.objectweb.asm.MethodVisitor{
    
        @java.lang.Deprecated
        public deprecated void visitMethodInsn(int,java.lang.String,java.lang.String,java.lang.String);
    }
    
    File: ./ls-annotations/build/libs/ls-annotations.jar - org/objectweb/asm/Opcodes.class
    
    public abstract interface org.objectweb.asm.Opcodes{
    
        @java.lang.Deprecated
        public static final deprecated int ASM10_EXPERIMENTAL = 17432576;}
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotations in classes within directory (recursive)</summary>

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes</code>

Output:
<pre>
    File: ./ls-annotations/build/classes/java/test/lsann/AppTest.class
    
    public class lsann.AppTest{
    
        @org.junit.Test
        public void testScanAnnotations();
    
    
        @org.junit.Test
        public void testShowDerived();
    
    
        @org.junit.Test
        public void testAnnotationUsage();
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/ClassWithAnnotations.class
    
    
    @org.springframework.boot.autoconfigure.SpringBootApplication(
        scanBasePackages={
            "com.max.b2c.*"})
    @org.springframework.context.annotation.ComponentScan(
        basePackages={
            "arg.a",
            "org.b"},
        excludeFilters={
            @org.springframework.context.annotation.ComponentScan$Filter(
                    type=org.springframework.context.annotation.FilterType.CUSTOM,
                    classes={
                        lsann.attrib.ClassWithAnnotations.class}),
            @org.springframework.context.annotation.ComponentScan$Filter(
                    type=org.springframework.context.annotation.FilterType.CUSTOM,
                    classes={
                        org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter.class})})
    public class lsann.attrib.ClassWithAnnotations{
    
        @org.springframework.context.annotation.Bean
        public lsann.attrib.ClassWithAnnotations outgoingMessageBuilderFactory(
                @org.springframework.beans.factory.annotation.Autowired
                java.util.List,
                @org.springframework.beans.factory.annotation.Value(
                    value="${external.s3.region}")
                java.lang.String,
                @org.springframework.beans.factory.annotation.Value(
                    value="${external.s3.bucket}")
                java.lang.String,
                @org.springframework.beans.factory.annotation.Value(
                    value="${external.s3.min-message-size-bytes}")
                int,
                @org.springframework.beans.factory.annotation.Value(
                    value="${external.s3.upload-part-size-bytes}")
                int);
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestDerived2.class
    
    public class lsann.attrib.TestDerived2
      extends lsann.attrib.TestDerivedClass{
    
        @lsann.attrib.TestAttrib4
         java.lang.String member;
        @lsann.attrib.TestAttrib4
        public void setMe(java.lang.String);
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestAttrib.class
    
    public @interface lsann.attrib.TestAttrib
      implements java.lang.annotation.Annotation{
        public abstract java.lang.String name();
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestBaseClass.class
    
    
    @lsann.attrib.TestAttrib4
    public class lsann.attrib.TestBaseClass{
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestAttrib4.class
    
    
    @lsann.attrib.TestAttrib2
    public @interface lsann.attrib.TestAttrib4
      implements java.lang.annotation.Annotation{
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestAttrib2.class
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib2
      implements java.lang.annotation.Annotation{
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/ClassWithAnnotations$NestedClass$NestedLevelTwoClass.class
    
    
    @org.springframework.stereotype.Component
    public class lsann.attrib.ClassWithAnnotations$NestedClass$NestedLevelTwoClass{
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/ClassWithAnnotations$NestedClass.class
    
    
    @org.springframework.stereotype.Component
    public class lsann.attrib.ClassWithAnnotations$NestedClass{
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestAttrib3.class
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib3
      implements java.lang.annotation.Annotation{
    }
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotation definitions that are used in definition of annotation @lsann.attrib.TestAttrib4</summary>

Command: <code>java -jar ls-annotations.jar -e @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes</code>

Output:
<pre>
    
    @lsann.attrib.TestAttrib2
    public @interface lsann.attrib.TestAttrib4
      implements java.lang.annotation.Annotation{
    }
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib2
      implements java.lang.annotation.Annotation{
    }
    
    public @interface lsann.attrib.TestAttrib
      implements java.lang.annotation.Annotation{
        public abstract java.lang.String name();
    }
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotation definitions that extend annotation @lsann.attrib.TestAttrib</summary>

Command: <code>java -jar ls-annotations.jar -a @lsann.attrib.TestAttrib ./ls-annotations/build/classes</code>

Output:
<pre>
    public @interface lsann.attrib.TestAttrib
      implements java.lang.annotation.Annotation{
        public abstract java.lang.String name();
    }
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib2
      implements java.lang.annotation.Annotation{
    }
    
    
    @lsann.attrib.TestAttrib2
    public @interface lsann.attrib.TestAttrib4
      implements java.lang.annotation.Annotation{
    }
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib3
      implements java.lang.annotation.Annotation{
    }
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all uses of @lsann.attrib.TestAttrib4 with highlight</summary>

Command: <code>java -jar ls-annotations.jar -u @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes</code>

Output:
<pre>
    public class lsann.attrib.TestDerived2
      extends lsann.attrib.TestDerivedClass{
    
        <b>@lsann.attrib.TestAttrib4</b>
         java.lang.String member;
        <b>@lsann.attrib.TestAttrib4</b>
        public void setMe(java.lang.String);
    }
    
    
    <b>@lsann.attrib.TestAttrib4</b>
    public class lsann.attrib.TestBaseClass{
    }
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all uses of @lsann.attrib.TestAttrib4, including use of derived annotations </summary>

Command: <code>java -jar ls-annotations.jar -w @lsann.attrib.TestAttrib ./ls-annotations/build/classes</code>

Output:
<pre>
    public @interface lsann.attrib.TestAttrib
      implements java.lang.annotation.Annotation{
        public abstract java.lang.String name();
    }
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib2
      implements java.lang.annotation.Annotation{
    }
    
    
    @lsann.attrib.TestAttrib2
    public @interface lsann.attrib.TestAttrib4
      implements java.lang.annotation.Annotation{
    }
    
    public class lsann.attrib.TestDerived2
      extends lsann.attrib.TestDerivedClass{
    
        <b>@lsann.attrib.TestAttrib4</b>
         java.lang.String member;
        <b>@lsann.attrib.TestAttrib4</b>
        public void setMe(java.lang.String);
    }
    
    
    <b>@lsann.attrib.TestAttrib4</b>
    public class lsann.attrib.TestBaseClass{
    }
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib3
      implements java.lang.annotation.Annotation{
    }
    

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all derived classes of java.lang.Object</summary>

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes -d java.lang.Object</code>

Output:
<pre>
    java.lang.Object
        lsann.AppTest lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@300ffa5d
        lsann.attrib.ClassWithAnnotations lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@1f17ae12
        lsann.attrib.ClassWithAnnotations$NestedClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@4d405ef7
        lsann.attrib.ClassWithAnnotations$NestedClass$NestedLevelTwoClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@6193b845
        lsann.attrib.TestAttrib lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@2e817b38
        lsann.attrib.TestAttrib2 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@c4437c4
        lsann.attrib.TestAttrib3 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@433c675d
        lsann.attrib.TestAttrib4 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@3f91beef
        lsann.attrib.TestBaseClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@1a6c5a9e
            lsann.attrib.TestDerivedClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@37bba400
                lsann.attrib.TestDerived2 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@179d3b25
        lsann.attrib.TestInterface lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@254989ff
            lsann.attrib.TestDerivedClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@37bba400
                lsann.attrib.TestDerived2 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@179d3b25

<pre>  
  
</details>

    
<hr>
<details>
<summary>show all base classes of lsann.attrib.TestDerived2</summary>

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes -b lsann.attrib.TestDerived2</code>

Output:
<pre>
    lsann.attrib.TestDerived2 lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@79fc0f2f
        lsann.attrib.TestDerivedClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@1f17ae12
            lsann.attrib.TestBaseClass lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@4d405ef7
                java.lang.Object
            lsann.attrib.TestInterface lsann.ClassHierarchyAsmClassVisitor$ClassEntryData@6193b845
                java.lang.Object

<pre>  
  
</details>

