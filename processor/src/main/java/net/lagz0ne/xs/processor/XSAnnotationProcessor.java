package net.lagz0ne.xs.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import net.lagz0ne.xs.annotation.Initializer;
import net.lagz0ne.xs.annotation.Service;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class XSAnnotationProcessor extends BasicAnnotationProcessor {

    private final ProcessingEnvironment env = processingEnv;
    private final Messager messenger = processingEnv.getMessager();
    private final Filer filer = processingEnv.getFiler();

    @Override protected Iterable<? extends ProcessingStep> initSteps() {
        return Lists.newArrayList(
                new GenerateNamedDependencyStep()
        );
    }

    private class GenerateNamedDependencyStep implements BasicAnnotationProcessor.ProcessingStep {

        @SuppressWarnings("all")
        @Override public Set<? extends Class<? extends Annotation>> annotations() {
            return Sets.newHashSet(
                    Service.class
            );
        }

        @Override public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
            processServiceAnnotation(elementsByAnnotation.get(Service.class));
            return ImmutableSet.of();
        }

        private void processServiceAnnotation(Set<Element> elements) {
            elements.forEach(this::processElement);
        }

        private void processElement(Element element) {
            List<? extends Element> enclosedElements = element.getEnclosedElements();
            messenger.printMessage(Diagnostic.Kind.NOTE, enclosedElements.toString());

        }
    }


}
