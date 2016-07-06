package net.lagz0ne.xs.processor;

import com.google.common.collect.Sets;
import net.lagz0ne.xs.annotation.Service;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

public class XSAnnotationProcessor extends AbstractProcessor {

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processServiceAnnotation(roundEnv.getElementsAnnotatedWith(Service.class));
        return false;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(Service.class.getCanonicalName());
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private void processServiceAnnotation(Set<? extends Element> elements) {
        elements.forEach(this::processElement);
    }

    private void processElement(Element element) {
        try {
            ResolverWriter.write((TypeElement) element, processingEnv);
        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }


}
