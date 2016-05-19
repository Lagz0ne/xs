package net.lagz0ne.xs.annotation.testFiles;

import net.lagz0ne.xs.ServiceDependencies;
import net.lagz0ne.xs.XSContainer;
import rx.Observable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static javafx.scene.input.KeyCode.T;

public final class AnnotatedService1Dependency implements ServiceDependencies {

    @Override public Class<?> getImplementationClass() {
        return AnnotatedService1.class;
    }

    @Override public Class<?> getInterfaceClasses() {
        return null;
    }

    @Override public Map<String, Class<?>> getNamedDependencies() {
        Map<String, Class<?>> dependencies = new HashMap<>();
        dependencies.put("service2", AnnotatedService2.class);
        return dependencies;
    }

    public Observable<AnnotatedService1> create(Map<String, Object> resolvedDependencies) {
        return Observable.fromCallable(AnnotatedService1::new)
                .doOnNext(instance -> {
                    AnnotatedService2 annotatedService2 = retrieveDependency(resolvedDependencies, "service2");
                    instance.service2 = annotatedService2;
                });
    }
}
