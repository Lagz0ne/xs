package net.lagz0ne.xs;

import rx.Observable;
import rx.subjects.AsyncSubject;

import java.lang.reflect.Field;
import java.util.*;

public class XSContainer {

    private Map<String, AsyncSubject<Object>> serviceCache = new HashMap<>();
    private List<ServiceDependencies> dependenciesList = new ArrayList<>();
    private XSContainer() {}

    public static XSContainer initialize() {
        return new XSContainer();
    }

    public XSContainer register(ServiceDependencies dependencies) {
        AsyncSubject<Object> instanceSubject = AsyncSubject.create();
        serviceCache.computeIfAbsent(dependencies.getImplementationClass().getName(), s -> instanceSubject);
//        serviceCache.computeIfAbsent(dependencies.getInterfaceClass().getName(), s -> instanceSubject);
        dependenciesList.add(dependencies);
        return this;
    }


    private Observable<InitializationResult> instantiateInstance(ServiceDependencies service) {

        AsyncSubject<Object> instanceSubject = serviceCache.get(service.getImplementationClass().getName());

        List<Observable<NamedDependency>> actualDependencies = new ArrayList<>();
        Map<String, Class<?>> requirements = service.getNamedDependencies();

        requirements.forEach((key, value) -> {
            Observable<NamedDependency> targetInstance = serviceCache.get(value.getName())
                    .map(inst -> new NamedDependency(key, inst));

            actualDependencies.add(targetInstance);
        });

        return Observable.merge(actualDependencies)
            .toList()
            .map(retrievedDependencies -> {
                try {
                    Object target = service.getImplementationClass().newInstance();
                    for (NamedDependency dep : retrievedDependencies) {
                        Field field = target.getClass().getField(dep.getName());
                        field.set(target, dep.getDependency());
                    }
                    return Optional.of(target);
                } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            })
            .doOnNext(target -> {
                target.ifPresent(instanceSubject::onNext);
                instanceSubject.onCompleted();
            })
            .map(target -> InitializationResult.succeded(service))
            .defaultIfEmpty(InitializationResult.neverCalled(service))
            .onErrorReturn(throwable -> InitializationResult.failedOnInitialization(service, throwable));
    }

    public XSContainer build() {
        long start = System.currentTimeMillis();
        List<ServiceDependencies> toBeBuilt = Collections.unmodifiableList(dependenciesList);

        Observable.from(toBeBuilt)
                .flatMap(this::instantiateInstance)
                .doOnNext(result -> {
                    if (!result.isFailed()) {
                        Logger.debug("Service {} is ✓", result.getDependency().getImplementationClass());
                    } else {
                        if (result.getCause() == null) {
                            Logger.error("Service {0} didn't have all dependencies", result.getDependency().getImplementationClass());
                            Logger.error("  Dependency tree {0}", result.getDependency().getNamedDependencies().values());
                        } else {
                            Logger.error("Service {0} comes with an exception");
                            Logger.error(result.getCause().toString(), result.getCause());
                        }
                    }
                })
                .doOnCompleted(() -> {
                    Logger.debug("-- Enjoy xs within {}ms --", (System.currentTimeMillis() - start) / 1000);
                })
                .subscribe();

        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> targetClass) {
        return (T) serviceCache.get(targetClass.getName()).getValue();
    }

}
