## ls-annotation

ls-annotation is a java bytecode decompiler; it extracts and prints definitions of classes, methods and fields that use java annotations.

The purpose of this tool is to make sense out of declarations with annotations that depend on one another, an example for this kind of dependencies is @Autowire from Spring Boot:
In Spring Boot a function that is marked with @Autowire will get the value returned by a function marked with the @Bean annotation, provided that this function
returns the same type or a derived class as that of the parameter type marked with @Autowire, and that the class with the Bean annotation is included in the @ComponentScan annotation.
The same mechanism is applied to constructor arguments of classe marked with the @Component annotation.

Now you can have some of the features of following up on annotations in IntelliJ, but this program is nice if you prefer to look at text.

The motivation for this tool is to extract all classes/methods that have an annotation - and do so based on the bytecode of a system. I think that searching through the bytecode is very effective way to look at complex java based systems; Searching through the source code is much more limited, often one is faced with the problem of chasing multiple dependencies and their versions (and one might have to do so across multiple source code repositories); one doesn't have this problem when looking at the complete binary byte code representation of a given system.

Limitations: this program can detect detect annotations placed into the byte code, it can detect annotations with retention policy CLASS and RUNTIME [link](https://docs.oracle.com/javase/7/docs/api/java/lang/annotation/RetentionPolicy.html). It can't detect annotations with retention policy SOURCE that are not put into bytecode, for example @Override is one of these.

## Installation 

Download ls-annotation.jar from the [release page](https://github.com/MoserMichael/ls-annotations/releases/)

you need JDK version 8 to run this program.

## Usage

ls-annotation is a command line utility;


## Command line options
<pre>
java -jar ls-annotations.jar [[-l|--list] className|[-b|--baseOf] className]|[[-d|--derivedOf]] [--scanRec|-r] [&lt;directory&gt;|&lt;jar file&gt;]*

Display all definitions with annotations
it works by scans by decompiling the bytecode of class files selectively to shows all definitions with annotations.
helps to decipher systems with interdependent annotations (like spring/grpc, etc)

Common Arguments:
 &lt;directory&gt;       scans directory recursively for jarsa nd object files to scan
 &lt;jar file&gt;        scans jar file
 --scanRec -r      scans jars contained in jars (default off) (optional)
 -v                (debug) very verbose, trace objectweb events

Commands:
 -l               List all annotated classes or methods and show the annotations
 --list

  -c              show constructors for classes with annotations (for -l)
  --showctor

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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotations in classes within directory (recursive)</summary>
  

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes</code>

<pre>
    File: ./ls-annotations/build/classes/java/test/lsann/AppTest.class
    
    public class lsann.AppTest{
    
        @org.junit.Test
        public void testScanAnnotations();
    
    
        @org.junit.Test
        public void testScanAnnotationsAndShowCtor();
    
    
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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotations in classes within directory (recursive), show constructor on classes with annotations</summary>
  

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes -c</code>

<pre>
    File: ./ls-annotations/build/classes/java/test/lsann/AppTest.class
    
    public class lsann.AppTest{
    
        @org.junit.Test
        public void testScanAnnotations();
    
    
        @org.junit.Test
        public void testScanAnnotationsAndShowCtor();
    
    
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
        public &lt;init&gt;(java.lang.String,java.lang.String,int,boolean);
    
    
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
        public &lt;init&gt;();
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
        public &lt;init&gt;(
                @org.springframework.beans.factory.annotation.Autowired
                java.lang.String);
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/ClassWithAnnotations$NestedClass.class
    
    
    @org.springframework.stereotype.Component
    public class lsann.attrib.ClassWithAnnotations$NestedClass{
        public &lt;init&gt;(
                @org.springframework.beans.factory.annotation.Autowired
                int);
    }
    
    File: ./ls-annotations/build/classes/java/test/lsann/attrib/TestAttrib3.class
    
    
    @lsann.attrib.TestAttrib(
        name="kuku")
    public @interface lsann.attrib.TestAttrib3
      implements java.lang.annotation.Annotation{
    }
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotation definitions that are used in definition of annotation @lsann.attrib.TestAttrib4</summary>
  

Command: <code>java -jar ls-annotations.jar -e @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes</code>

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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all annotation definitions that extend annotation @lsann.attrib.TestAttrib</summary>
  

Command: <code>java -jar ls-annotations.jar -a @lsann.attrib.TestAttrib ./ls-annotations/build/classes</code>

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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all uses of @lsann.attrib.TestAttrib4 with highlight</summary>
  

Command: <code>java -jar ls-annotations.jar -u @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes</code>

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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all uses of @lsann.attrib.TestAttrib4, including use of derived annotations </summary>
  

Command: <code>java -jar ls-annotations.jar -w @lsann.attrib.TestAttrib ./ls-annotations/build/classes</code>

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
    

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all derived classes of java.lang.Object</summary>
  

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes -d java.lang.Object</code>

<pre>
    java.lang.Object
        lsann.AllJarClassVisitors [public class]
        lsann.AllJarClassVisitors$1 [class]
        lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$1 [class]
        lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$ShowAnnotationUsage [public class]
        lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$ShowUsageRecursive [public class]
        lsann.AppTest [public class]
        lsann.AstDefinition [public class]
        lsann.AstDefinition$AnnotationBaseRep [public abstract class]
            lsann.AstDefinition$AnnotationCompoundRep [public class]
                lsann.AstDefinition$AnnotationArrayRep [public class]
                lsann.AstDefinition$AnnotationRep [public class]
                    lsann.AstDefinition$AnnotationNestedRep [public class]
            lsann.AstDefinition$AnnotationEnumValRep [public class]
            lsann.AstDefinition$AnnotationValueRep [public class]
        lsann.AstDefinition$RepBase [public abstract class]
            lsann.AstDefinition$ClassRep [public class]
            lsann.AstDefinition$FieldRep [public class]
            lsann.AstDefinition$MethodParamRep [public class]
            lsann.AstDefinition$MethodRep [public class]
        lsann.AstVisitorEvents [public abstract class]
            lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$AnnoDeclGraphAstVisitorEvents [class]
            lsann.AllJarClassVisitors$LsAnnotationJarClassVisitor$LsAstVisitorEvents [class]
        lsann.ClassHierarchyAsmClassVisitor$ClassEntryData [public class]
        lsann.SpringBootAutowireAnalyser [public class]
        lsann.asmtools.AsmAccessNames [public class]
        lsann.asmtools.AsmAccessNames$Entry [class]
        lsann.asmtools.SigParse [public class]
        lsann.asmtools.SigParse$PosParse [class]
        lsann.asmtools.TracingVisitors [public class]
        lsann.attrib.ClassWithAnnotations [public class]
        lsann.attrib.ClassWithAnnotations$NestedClass [public class]
        lsann.attrib.ClassWithAnnotations$NestedClass$NestedLevelTwoClass [public class]
        lsann.attrib.TestAttrib [public abstract @interface interface]
        lsann.attrib.TestAttrib2 [public abstract @interface interface]
        lsann.attrib.TestAttrib3 [public abstract @interface interface]
        lsann.attrib.TestAttrib4 [public abstract @interface interface]
        lsann.attrib.TestBaseClass [public class]
            lsann.attrib.TestDerivedClass [public class]
                lsann.attrib.TestDerived2 [public class]
        lsann.attrib.TestInterface [public abstract interface]
            lsann.attrib.TestDerivedClass [public class]
                lsann.attrib.TestDerived2 [public class]
        lsann.cmd.App [public class]
        lsann.fileio.JarClassVisitor [public abstract class]
            lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor [public class]
            lsann.AllJarClassVisitors$ClassHierarchyJarClassVisitor [public class]
            lsann.AllJarClassVisitors$LsAnnotationJarClassVisitor [public class]
            lsann.cmd.App$1 [class]
        lsann.fileio.JarReader [public class]
        lsann.fileio.JarReader$1 [class]
        lsann.fileio.JarReader$PathName [class]
        lsann.graph.HierarchyGraph [public class]
        lsann.graph.HierarchyGraph$Entry [public class]
        lsann.graph.HierarchyGraph$HierarchyGraphVisitor [public abstract interface]
            lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$1 [class]
            lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$ShowAnnotationUsage [public class]
            lsann.AllJarClassVisitors$AnnoDeclGraphJarClassVisitor$ShowUsageRecursive [public class]
            lsann.graph.HierarchyGraphVisitors$ShowHierarchyVisitor [public class]
        lsann.graph.HierarchyGraph$LinkEntry [class]
        lsann.graph.HierarchyGraphVisitors [public class]
        lsann.graph.HierarchyGraphVisitors$ShowHierarchyVisitor [public class]
        lsann.util.Pair [public class]
        lsann.util.StrUtil [public class]

</pre>  
  
</details>

    
<hr>
<details>
<summary>show all base classes of lsann.attrib.TestDerived2</summary>
  

Command: <code>java -jar ls-annotations.jar -l ./ls-annotations/build/classes -b lsann.attrib.TestDerived2</code>

<pre>
    lsann.attrib.TestDerived2 [public class]
        lsann.attrib.TestDerivedClass [public class]
            lsann.attrib.TestBaseClass [public class]
                java.lang.Object
            lsann.attrib.TestInterface [public abstract interface]
                java.lang.Object

</pre>  
  
</details>

## what i learned while writing this project

I wrote this tool as part of relearning java; java8 is quite an improvement over the old java that i used to work with. It took me some time and practice to get used to java streams. I think that the streams library was very much inspired by Scala futures.

This tool uses the [asm library](https://asm.ow2.io/) to scan class files and to extract annotations. you need to pass an event listener object to the asm library that derives from an event abstract base class and not from an interface in order to specify the aspects that you want to process; now it turns out that java abstract classes are much more versatile than interfaces.

Overall i was surprised at the many changes in the java language that took place during the last decade. I think Scala is loosing a bit of it's edge over java as a result. Now it turns out that they got Kotlin nowadays as a better/functional java, and it sort of wins over Scala due to better interoperability with java: Android picked Kotlin as it's most favoured language, because they just can't switch easily to a newer Jdk and language version, whereas kotlin is adding many of the newer language features that were inspired by scala and its functional programming style, while still maintaining interoperability with existing libraries written in java.

On the downside: kotlin doesn't have a pattern matching feature similar to that in Scala; actually Scala copied this feature from OCaml. Kotlin doesn't have the implicite keyword, this I think is a win because implicit doesn't help to write more readable code.

Overall there are quite a lot of changes in JDK land. Some ten years ago Java seemed to be old and settled in its ways, now suddenly everything looks quite different - when I look at it after a ten years brake

Also I learned that Scala is adding a lot of type information into the bytecode, scala has a type system that has more features than java; this is done by adding its own annotation class: scala.reflect.ScalaSignature - the scala compiler uses this annotation to encode the scala signature in pickled form, so that the annotation value includes binary data. This is a bit of a hack that allows to make the scala language more expressive, as type information about the added features is pushed into this annotation, however it doesn't help with java interoperability. See [link](https://stackoverflow.com/questions/29267199/what-is-a-scalasignature). I think that's an interesting  detail of the jdk language wars.

Also interesting that all the newer languages like Scala and Kotlin are all getting rid of Java primitive types like int and long; in both Scala and Kotlin everything is an object. Now this change apparentlty makes it impossible to have compact in memory data structures: without primitive types each class member becomes a reference to a different object! That would result in much more processor cache misses in stuff written in the newer jdk languages.

