package net.lagz0ne.xs.sample;

import net.lagz0ne.xs.ServiceDependencies;

import java.util.HashMap;
import java.util.Map;

public class Service2ImplDependency implements ServiceDependencies {
    @Override
    public Class getImplementationClass() {
        return Service2Impl.class;
    }

    @Override
    public Class getInterfaceClass() {
        return ServiceInterface2.class;
    }

    @Override
    public Map<String, Class<?>> getNamedDependencies() {
        HashMap<String, Class<?>> dependencies = new HashMap<>();
        dependencies.put("service1", ServiceInterface1.class);

        return dependencies;
    }
}
