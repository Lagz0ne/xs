package net.lagz0ne.xs;

import rx.Observable;
import rx.subjects.AsyncSubject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XSContainer {

    private Map<Class, AsyncSubject<?>> serviceCache = new ConcurrentHashMap<>();
    private Map<Class, List<AsyncSubject<?>>> interfaceCache = new ConcurrentHashMap<>();

    // User ServiceLoader for this
    private final Iterable<Resolver> dependenciesList;

    private XSContainer() {
        dependenciesList = ServiceLoader.load(Resolver.class);
        this.init();
    }

    private void init() {
        for (Resolver resolver : dependenciesList) {
            Collection deps = resolver.dependencies().values();

            Observable<String> keys = Observable
                    .from(resolver.dependencies().keySet());

            Observable<LinkedHashMap<String, Object>> finalMap = Observable.just(new LinkedHashMap<>());

            Observable<Object> resolveds = Observable.from(deps)
                    .flatMap(dependency -> serviceCache.get(dependency));

            finalMap.flatMap($finalMap ->
                    Observable.zip(keys.toList(), resolveds.toList(), ($keys, $resolveds) -> {
                        for (int i = 0; i < $keys.size(); i++) {
                            $finalMap.put($keys.get(i), $resolveds.get(i));
                        }
                        return Observable.fromCallable(() -> resolver.onResolved($finalMap));
                    }
                ));
        }
    }

    public static XSContainer initialize() {
        return new XSContainer();
    }

}
