package net.lagz0ne.xs;

import java.util.Map;

public interface ServiceDependencies {

    Class<?> getImplementationClass();
    Class<?> getInterfaceClasses();
    Map<String, Class<?>> getNamedDependencies();

    @SuppressWarnings("unchecked")
    default <T> T retrieveDependency(Map<String, Object> resolvedDependencies, String name) {
        return (T) resolvedDependencies.get(name);
    }
}
