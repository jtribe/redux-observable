package au.com.jtribe.reduxobservable.reduxkotlin;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import redux.MiddlewareKt;
import redux.StoreKt;
import redux.api.Dispatcher;
import redux.api.Store;
import redux.api.enhancer.Middleware;

public class EpicMiddlewareTest {

  static class TestMiddleware implements Middleware<Integer> {
    private PublishSubject<Object> actionSubject;

    public TestMiddleware(PublishSubject<Object> actionSubject) {
      this.actionSubject = actionSubject;
    }

    @Override public Object dispatch(Store<Integer> store, Dispatcher next, Object action) {
      actionSubject.onNext(action);
      return next.dispatch(action);
    }
  }

  @Test
  public void testMiddleware() {
    PublishSubject<Object> actionSubject = PublishSubject.create();

    Middleware middleware =
        EpicMiddlewareFactory.createMiddleware(
            (actions, stateProvider) -> actions.ofType(BeepAction.class)
                .map(__ -> new BoopAction()));

    TestMiddleware testMiddleware = new TestMiddleware(actionSubject);

    Store store = StoreKt.createStore(new StateReducer(), 1,
        MiddlewareKt.applyMiddleware(middleware, testMiddleware));

    TestObserver<Object> testObserver = actionSubject.test();
    testObserver.assertValueCount(0);

    store.dispatch(new BeepAction());
    testObserver.assertValueCount(2);
  }
}
