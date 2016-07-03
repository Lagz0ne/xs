package annotation;

import net.lagz0ne.xs.Resolver;

import java.util.LinkedHashMap;

public class AnnotatedService2Resolver implements Resolver<AnnotatedService2> {

    private static final LinkedHashMap<String, Class<?>> DEPENDENCIES = new LinkedHashMap<String, Class<?>>() {{}};
    private static final MODE mode = MODE.FIELD;

    @Override public Class<AnnotatedService2> getConcreteClass() {
        return AnnotatedService2.class;
    }

    @Override public Class<?>[] getInterfaces() {
        return null;
    }

    @Override public LinkedHashMap<String, Class<?>> dependencies() {
        return DEPENDENCIES;
    }

    @Override public AnnotatedService2 onResolved(LinkedHashMap<String, Object> resolvedDependencies) {
        return new AnnotatedService2();
    }

}
