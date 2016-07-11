package net.lagz0ne.xs;

import rx.Observable;
import rx.subjects.AsyncSubject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import static rx.Observable.from;
import static rx.Observable.fromCallable;

public class XSContainer {

    private Map<Class, AsyncSubject<Object>> SERVICE_CACHE = new ConcurrentHashMap<>();
    private Map<Class, List<AsyncSubject<?>>> interfaceCache = new ConcurrentHashMap<>();
    private Map<Class, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();

    // User ServiceLoader for this
    private final Iterable<Resolver> resolvers;

    private XSContainer() {
        resolvers = ServiceLoader.load(Resolver.class);
        this.init();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        Observable.merge(instantiateEmptyDeps(), instantiateRemainingDeps())
                .subscribe();
    }

    private Observable instantiateRemainingDeps() {
        return from(resolvers)
                .filter(resolver -> !resolver.dependencies().isEmpty())
                .flatMap(resolver -> {
                    Observable<Object> resolvedDependencies = from(resolver.dependencies().values()).flatMap(SERVICE_CACHE::get);
                    Observable<String> dependencyNames = from(resolver.dependencies().keySet());

                    LinkedHashMap<String, Object> resolvedDependenciesMap = new LinkedHashMap<>();
                    return Observable.zip(dependencyNames, resolvedDependencies, (name, instance) -> {
                        resolvedDependenciesMap.put(name, instance);
                        return resolvedDependenciesMap;
                    })
                            .last()
                            .flatMap(deps -> fromCallable(() -> {
                                Object instance = resolver.onResolved(deps);
                                AsyncSubject<Object> instanceLauncher = SERVICE_CACHE.computeIfAbsent(resolver.getConcreteClass(), key -> AsyncSubject.create());
                                instanceLauncher.onNext(instance);
                                instanceLauncher.onCompleted();
                                INSTANCE_CACHE.put(resolver.getConcreteClass(), instance);
                                return instance;
                            }));
                });
    }

    private Observable instantiateEmptyDeps() {
        return from(resolvers)
                .filter(resolver -> resolver.dependencies().isEmpty())
                .flatMap(resolver -> fromCallable(() -> {
                            AsyncSubject<Object> instance = AsyncSubject.create();
                            SERVICE_CACHE.put(resolver.getConcreteClass(), instance);
                            if (resolver.dependencies().isEmpty()) {
                                Object actualInstance = resolver.onResolved(new LinkedHashMap<>());
                                instance.onNext(actualInstance);
                                instance.onCompleted();
                                INSTANCE_CACHE.put(resolver.getConcreteClass(), actualInstance);
                            }
                            return resolver;
                        }
                ));
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) INSTANCE_CACHE.get(clazz);
    }

    public static XSContainer initialize() {
        return new XSContainer();
    }

}
