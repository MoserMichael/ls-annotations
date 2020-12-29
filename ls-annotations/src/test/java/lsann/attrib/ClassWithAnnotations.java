package lsann.attrib;

import java.util.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


//Disclaimer: these annotations usages make no sense at all
@SpringBootApplication(scanBasePackages = "com.max.b2c.*")
@ComponentScan(
        basePackages = {
                "arg.a",
                "org.b" },
        excludeFilters = {
                @Filter(
                        type = FilterType.CUSTOM,
                        classes = ClassWithAnnotations.class),
                @Filter(
                        type = FilterType.CUSTOM,
                        classes = AutoConfigurationExcludeFilter.class) })
public class ClassWithAnnotations {

    @Bean
    public ClassWithAnnotations outgoingMessageBuilderFactory(
            @Autowired List<String> aaa,
            @Value("${external.s3.region}") String regionName,
            @Value("${external.s3.bucket}") String bucketName,
            @Value("${external.s3.min-message-size-bytes}") int minMessageSize,
            @Value("${external.s3.upload-part-size-bytes}") int uploadPartSize)
    {
        return null;
    }

    // nested classes that extend a class get their own visit calls in objectweb.
    // nested classes without base class get a visitInnerClass call; that's a big ambiguous, if you ask me.
    // also second level nested classes get their own calls.
    @Component
    public static class NestedClass { //extends TestBaseClass {
        public NestedClass(@Autowired int foo) {
        }

        @Component
        public static class NestedLevelTwoClass {
            public NestedLevelTwoClass(@Autowired String moo) {
            }

        }
    }

    public void functionAfterNestedClasses() {
    }

}
