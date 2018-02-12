package au.com.jtribe.reduxobservable.middlewaretests;

import au.com.jtribe.reduxobservable.func.StateProvider;
import au.com.jtribe.reduxobservable.redux.Epic;
import au.com.jtribe.reduxobservable.redux.EpicMiddlewareFactory;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import org.junit.Assert;
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
            new Epic() {
              @Override
              public Observable<Object> map(Observable actions, StateProvider stateProvider) {
                return actions.ofType(BeepAction.class)
                    .map(new Function<BeepAction, Object>() {
                      @Override public Object apply(BeepAction beepAction) throws Exception {
                        return new BoopAction();
                      }
                    });
              }
            });

    TestMiddleware testMiddleware = new TestMiddleware(actionSubject);

    State s = new State(0);

    //noinspection unchecked
    Store<State> store = StoreKt.createStore(new StateReducer(), s,
        MiddlewareKt.applyMiddleware(middleware, testMiddleware));

    TestObserver<Object> testObserver = actionSubject.test();
    testObserver.assertValueCount(0);

    store.dispatch(new BeepAction());
    testObserver.assertValueCount(2);

    Assert.assertEquals("The expected value is -1", -1, store.getState().i);
  }

  @Test
  public void testMultipleMiddleware() {

    PublishSubject<Object> actionSubject = PublishSubject.create();
    final long[] firstEpicInvocations = {0};
    final long[] secondEpicInvocations = {0};

    final BeepAction beepAction = new BeepAction();
    final BoopAction boopAction = new BoopAction();
    final BaapAction baapAction = new BaapAction();

    Epic<State> firstEpic = new Epic<State>() {

      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<State> stateProvider) {
        return actions.ofType(BeepAction.class)
            .doOnNext(new Consumer<BeepAction>() {
              @Override public void accept(BeepAction __) throws Exception {
                firstEpicInvocations[0] += 1;
              }
            })
            .map(new Function<BeepAction, Object>() {
              @Override public Object apply(BeepAction __) throws Exception {
                return boopAction;
              }
            });
      }
    };

    Epic<State> secondEpic = new Epic<State>() {

      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<State> stateProvider) {
        return actions.ofType(BoopAction.class)
            .doOnNext(new Consumer<BoopAction>() {
              @Override public void accept(BoopAction __) throws Exception {
                secondEpicInvocations[0] += 1;
              }
            })
            .map(new Function<BoopAction, Object>() {
              @Override public Object apply(BoopAction __) throws Exception {
                return baapAction;
              }
            });
      }
    };

    Middleware<State> m = EpicMiddlewareFactory.createMiddleware(firstEpic);
    Middleware<State> m2 = EpicMiddlewareFactory.createMiddleware(secondEpic);

    Middleware<State> testMiddleware = new TestMiddleware(actionSubject);

    TestObserver<Object> testSubscriber =
        actionSubject.skip(1).test(); // we skip the initial Object that gets emitted -- that just initialises the store and we don't care about it

    //noinspection unchecked
    Store<State> store = StoreKt.createStore(new StateReducer(), new State(0),
        MiddlewareKt.applyMiddleware(m, m2, testMiddleware));

    assertEquals("The first middleware has been invoked zero times", firstEpicInvocations[0], 0);
    assertEquals("The second middleware has been invoked zero times", secondEpicInvocations[0], 0);
    Assert.assertEquals("The value in the state is equal to 0", 0, store.getState().i);


    store.dispatch(boopAction);
    assertEquals("The first middleware has been invoked zero times", 0, firstEpicInvocations[0]);
    assertEquals("The second middleware has been invoked one time", 1, secondEpicInvocations[0]);
    Assert.assertEquals("The value in the state is equal to -2", -2, store.getState().i);

    store.dispatch(beepAction);
    assertEquals("The first middleware has been invoked one time", 1, firstEpicInvocations[0]);
    assertEquals("The second middleware has been invoked two times", 2, secondEpicInvocations[0]);
    Assert.assertEquals("The value in the state is equal to -3", -3, store.getState().i);

    store.dispatch(baapAction);
    assertEquals("The first middleware has been invoked one time", 1, firstEpicInvocations[0]);
    assertEquals("The second middleware has been invoked two times", 2, secondEpicInvocations[0]);
    Assert.assertEquals("The value in the state is equal to -3", -3, store.getState().i);


    testSubscriber.assertValues(boopAction, baapAction, beepAction, boopAction, baapAction, baapAction);
  }
}
