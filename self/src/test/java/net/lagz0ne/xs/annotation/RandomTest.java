package net.lagz0ne.xs.annotation;

import net.lagz0ne.xs.XSContainer;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class RandomTest {

    @Test
    public void random() {
        XSContainer container = XSContainer.initialize();
        Assertions.assertThat(container.getInstance(AnnotatedService1.class)).isNotNull();
    }

}
