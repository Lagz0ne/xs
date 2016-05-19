package net.lagz0ne.xs.annotation;

import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import net.lagz0ne.xs.processor.XSAnnotationProcessor;
import org.junit.Test;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class TestAnnotatedService {

    @Test
    public void shouldBeAbleToCompile() {
        Truth.assert_().about(javaSource())
                .that(JavaFileObjects.forResource("annotation/AnnotatedService1.java"))
                .processedWith(new XSAnnotationProcessor())
                .compilesWithoutError();
    }
}
