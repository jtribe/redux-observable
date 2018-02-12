package au.com.jtribe.reduxobservable.middlewaretests;

import io.reactivex.subjects.PublishSubject;
import redux.api.Dispatcher;
import redux.api.Store;
import redux.api.enhancer.Middleware;

class TestMiddleware implements Middleware<State> {
  private PublishSubject<Object> actionSubject;

  public TestMiddleware(PublishSubject<Object> actionSubject) {
    this.actionSubject = actionSubject;
  }

  @Override public Object dispatch(Store<State> store, Dispatcher next, Object action) {
    actionSubject.onNext(action);
    return next.dispatch(action);
  }
}
