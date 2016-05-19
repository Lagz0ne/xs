package net.lagz0ne.xs.annotation;

import com.squareup.javapoet.JavaFile;
import net.lagz0ne.xs.processor.ResolverWriter;
import org.junit.Test;

import java.io.IOException;


public class TestResolverWriter {

    @Test
    public void outputShouldMatch() throws IOException {
        new ResolverWriter().write("annotation", "AnnotatedService1").writeTo(System.out);
    }
}
