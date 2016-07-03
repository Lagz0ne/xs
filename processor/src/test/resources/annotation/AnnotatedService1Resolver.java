package annotation;

import net.lagz0ne.xs.Resolver;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;

public class AnnotatedService1Resolver implements Resolver<AnnotatedService1> {

    private static final LinkedHashMap<String, Class<?>> DEPENDENCIES = new LinkedHashMap<String, Class<?>>() {{
        this.put("service2", AnnotatedService2.class);
    }};

    @Override public Class<AnnotatedService1> getConcreteClass() {
        return AnnotatedService1.class;
    }

    @Override public LinkedHashMap<String, Class<?>> dependencies() {
        return DEPENDENCIES;
    }

    @Override public AnnotatedService1 onResolved(LinkedHashMap<String, Object> resolvedDependencies) throws Exception {
        Constructor<AnnotatedService1> constructor = getConcreteClass().getConstructor(dependencies().values().toArray(new Class[]{}));
        return constructor.newInstance(resolvedDependencies.values().toArray());
    }
}
