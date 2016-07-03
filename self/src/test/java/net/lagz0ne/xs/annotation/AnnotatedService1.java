package net.lagz0ne.xs.annotation;

import javax.inject.Inject;

@Service
public class AnnotatedService1 {

    @Inject AnnotatedService2 service2;

}
