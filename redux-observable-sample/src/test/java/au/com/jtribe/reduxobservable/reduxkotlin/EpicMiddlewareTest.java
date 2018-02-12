package au.com.jtribe.reduxobservable.reduxkotlin;

import au.com.jtribe.reduxobservable.func.StateProvider;
import au.com.jtribe.reduxobservable.redux.Epic;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Test;
import redux.MiddlewareKt;
import redux.StoreKt;
import redux.api.Store;
import redux.api.enhancer.Middleware;

import static org.junit.Assert.assertEquals;

public class EpicMiddlewareTest {

  @Test
  public void testMiddleware() {
    PublishSubject<Object> actionSubject = PublishSubject.create();

    Middleware middleware =
        EpicMiddlewareFactory.createMiddleware(
            (actions, stateProvider) -> actions.ofType(BeepAction.class)
                .map(__ -> new BoopAction()));

    TestMiddleware testMiddleware = new TestMiddleware(actionSubject);

    State s = new State(0);

    //noinspection unchecked
    Store<State> store = StoreKt.createStore(new StateReducer(), s,
        MiddlewareKt.applyMiddleware(middleware, testMiddleware));

    TestObserver<Object> testObserver = actionSubject.test();
    testObserver.assertValueCount(0);

    store.dispatch(new BeepAction());
    testObserver.assertValueCount(2);

    assertEquals("The expected value is -1", -1, store.getState().i);
  }

  @Test
  public void testMultipleMiddleware() {

    PublishSubject<Object> actionSubject = PublishSubject.create();
    final long[] firstEpicInvocations = {0};
    final long[] secondEpicInvocations = {0};

    BeepAction beepAction = new BeepAction();
    BoopAction boopAction = new BoopAction();
    BaapAction baapAction = new BaapAction();

    Epic<State> firstEpic = new Epic<State>() {

      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<State> stateProvider) {
        return actions.ofType(BeepAction.class)
            .doOnNext(__ -> firstEpicInvocations[0] += 1)
            .map(__ -> {
              return boopAction;
            });
      }
    };

    Epic<State> secondEpic = new Epic<State>() {

      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<State> stateProvider) {
        return actions.ofType(BoopAction.class)
            .doOnNext(__ -> secondEpicInvocations[0] += 1)
            .map(__ -> {
              return baapAction;
            });
      }
    };

    Middleware<State> m = EpicMiddlewareFactory.createMiddleware(firstEpic);
    Middleware<State> m2 = EpicMiddlewareFactory.createMiddleware(secondEpic);

    Middleware<State> testMiddleware = new TestMiddleware(actionSubject);

    TestObserver<Object> testSubscriber =
        actionSubject.skip(1).test(); // we skip the initial Object that gets emitted -- that just initialises the store and we don't care about it

    //noinspection unchecked
    Store store = StoreKt.createStore(new StateReducer(), new State(0),
        MiddlewareKt.applyMiddleware(m, m2, testMiddleware));

    assertEquals("The first middleware has been invoked zero times", firstEpicInvocations[0], 0);
    assertEquals("The second middleware has been invoked zero times", secondEpicInvocations[0], 0);

    store.dispatch(boopAction);
    assertEquals("The first middleware has been invoked zero times", firstEpicInvocations[0], 0);
    assertEquals("The second middleware has been invoked one time", secondEpicInvocations[0], 1);

    store.dispatch(beepAction);
    assertEquals("The first middleware has been invoked one time", firstEpicInvocations[0], 1);
    assertEquals("The second middleware has been invoked two times", secondEpicInvocations[0], 2);

    store.dispatch(baapAction);
    assertEquals("The first middleware has been invoked one time", firstEpicInvocations[0], 1);
    assertEquals("The second middleware has been invoked two times", secondEpicInvocations[0], 2);


    testSubscriber.assertValues(boopAction, baapAction, beepAction, boopAction, baapAction, baapAction);
  }
}
