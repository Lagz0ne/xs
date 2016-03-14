package net.lagz0ne.xs;

public class NamedDependency {

    private final String name;
    private final Object dependency;

    NamedDependency(String name, Object dependency) {
        this.name = name;
        this.dependency = dependency;
    }

    public String getName() {
        return name;
    }

    public Object getDependency() {
        return dependency;
    }
}
