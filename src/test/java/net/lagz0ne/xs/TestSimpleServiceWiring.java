package net.lagz0ne.xs;

import net.lagz0ne.xs.sample.Service1ImplDependency;
import net.lagz0ne.xs.sample.Service2Impl;
import net.lagz0ne.xs.sample.Service2ImplDependency;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestSimpleServiceWiring {

    @BeforeClass
    public static void setup() {
        Logger.getGlobal().setLevel(Level.ALL);
    }

    @Test
    public void serviceShouldBeAbleToWire() {
        XSContainer container = XSContainer.initialize();
        container.register(new Service1ImplDependency());
        container.register(new Service2ImplDependency());
        container.build();

        Assertions.assertThat(container.getService(Service2Impl.class)).isNotNull();
        Assertions.assertThat(container.getService(Service2Impl.class).service1).isNotNull();
    }

    @Test
    public void orderDoesntMatter() {
        XSContainer container = XSContainer.initialize();
        container.register(new Service2ImplDependency());
        container.register(new Service1ImplDependency());
        container.build();

        Assertions.assertThat(container.getService(Service2Impl.class)).isNotNull();
        Assertions.assertThat(container.getService(Service2Impl.class).service1).isNotNull();
    }

}
