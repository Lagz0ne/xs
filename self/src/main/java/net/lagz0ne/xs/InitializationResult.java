package net.lagz0ne.xs;

public class InitializationResult {
    private boolean failed;
    private ServiceDependencies dependency;
    private Throwable cause;

    private InitializationResult(boolean isFailed, Throwable throwable, ServiceDependencies dependency) {
        this.failed = isFailed;
        this.cause = throwable;
        this.dependency = dependency;
    }

    public static InitializationResult neverCalled(ServiceDependencies dependency) {
        return new InitializationResult(true, null, dependency);
    }

    public static InitializationResult failedOnInitialization(ServiceDependencies dependency, Throwable throwable) {
        return new InitializationResult(true, throwable, dependency);
    }

    public static InitializationResult succeded(ServiceDependencies dependency) {
        return new InitializationResult(false, null, dependency);
    }

    public boolean isFailed() {
        return failed;
    }

    public ServiceDependencies getDependency() {
        return dependency;
    }

    public Throwable getCause() {
        return cause;
    }
}
