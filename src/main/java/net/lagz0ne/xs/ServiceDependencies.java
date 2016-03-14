package net.lagz0ne.xs;

import java.util.Map;

public interface ServiceDependencies {

    Class<?> getImplementationClass();
    Class<?> getInterfaceClass();
    Map<String, Class<?>> getNamedDependencies();

}
