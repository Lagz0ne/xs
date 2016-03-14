package net.lagz0ne.xs.sample;

import javax.inject.Inject;

public class Service1Impl implements ServiceInterface1 {

    @Inject private ServiceInterface2 service2;
}
