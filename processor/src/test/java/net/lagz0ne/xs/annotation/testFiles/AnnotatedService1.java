package net.lagz0ne.xs.annotation.testFiles;

import net.lagz0ne.xs.annotation.Service;
import javax.inject.Inject;

@Service
public class AnnotatedService1 {

    @Inject AnnotatedService2 service2;

}
