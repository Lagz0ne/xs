package net.lagz0ne.xs;

import java.util.LinkedHashMap;

public interface Resolver<E> {

    /**
     * Registering implementation class
     */
    Class<E> getConcreteClass();

    /**
     * LinkedHashMap to reserve order, so it works in both constructor creator as well as field/method injections
     */
    LinkedHashMap<String, Class> dependencies();

    /**
     * Why should it be observable?
     */
    E onResolved(LinkedHashMap<String, Object> resolvedDependencies) throws Exception;

}