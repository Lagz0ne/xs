package net.lagz0ne.xs.annotation.testFiles;

import rx.Observable;

public class AnnotatedService2Creator {


    public Observable<AnnotatedService2> create() {

        return Observable.fromCallable(AnnotatedService2::new);
    }
}
