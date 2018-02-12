package au.com.jtribe.reduxobservable.epictests;

import au.com.jtribe.reduxobservable.exceptions.ReduxObservableException;
import au.com.jtribe.reduxobservable.func.StateProvider;
import au.com.jtribe.reduxobservable.redux.CombinedEpic;
import au.com.jtribe.reduxobservable.redux.Epic;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertTrue;

public class CombineEpicsTest {

  @Rule public ExpectedException expectedExceptionRule = ExpectedException.none();

  private class PingAction {
  }

  private class PongAction {
  }

  private class FinishPingPongAction {
  }

  private class StoreAccessedAction {
  }

  private class AccessStoreAction {
  }

  @Test
  public void testMakeEpics() {

    PublishSubject<Object> actions = PublishSubject.create();
    Epic pingEpic = new Epic<Object>() {
      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<Object> stateProvider) {
        return actions.ofType(PingAction.class).map(new Function<PingAction, Object>() {
          @Override public Object apply(PingAction __) throws Exception {
            return new PongAction();
          }
        });
      }
    };

    Epic pongEpic = new Epic<Object>() {
      @Override
      public Observable<Object> map(Observable<Object> actions,
          StateProvider<Object> stateProvider) {
        return actions.ofType(PongAction.class).map(new Function<PongAction, Object>() {
          @Override public Object apply(PongAction __) throws Exception {
            return new FinishPingPongAction();
          }
        });
      }
    };

    Epic combinedEpic = CombinedEpic.combineEpics(pingEpic, pongEpic);
    TestObserver<Object> testObserver = combinedEpic.map(actions, null).test();

    actions.onNext(new PingAction());
    testObserver.assertValueAt(0, new Predicate<Object>() {
      @Override public boolean test(Object v) throws Exception {
        return v instanceof PongAction;
      }
    });
    testObserver.assertValueCount(1);

    actions.onNext(new PongAction());
    testObserver.assertValueAt(1, new Predicate<Object>() {
      @Override public boolean test(Object v) throws Exception {
        return v instanceof FinishPingPongAction;
      }
    });
    testObserver.assertValueCount(2);
  }

  @Test
  public void testCanAccessStore() {
    PublishSubject<Object> actions = PublishSubject.create();
    final boolean[] accessed = {false};

    Epic accessStoreEpic = new Epic<Object>() {
      @Override
      public Observable<Object> map(Observable<Object> actions,
          final StateProvider<Object> stateProvider) {
        return actions.ofType(AccessStoreAction.class)
            .doOnNext(new Consumer<AccessStoreAction>() {
              @Override public void accept(AccessStoreAction __)
                  throws Exception {
                stateProvider.getState();
              }
            })
            .map(new Function<AccessStoreAction, Object>() {
              @Override public Object apply(AccessStoreAction __)
                  throws Exception {
                return new StoreAccessedAction();
              }
            });
      }
    };

    StateProvider stateProvider = new StateProvider() {
      @Override public Object getState() {
        accessed[0] = true;
        return null;
      }
    };

    TestObserver<Object> testObserver = accessStoreEpic.map(actions, stateProvider).test();

    actions.onNext(new AccessStoreAction());

    testObserver.assertValueCount(1);
    testObserver.assertValueAt(0, new Predicate<Object>() {
      @Override public boolean test(Object v) throws Exception {
        return v instanceof StoreAccessedAction;
      }
    });

    assertTrue("We've accessed the state", accessed[0]);
  }

  @Test
  public void nullEpicProvided() {
    expectedExceptionRule.expect(ReduxObservableException.class);
    expectedExceptionRule.expectMessage("You haven't provided any epics");

    CombinedEpic.combineEpics((ArrayList<Epic<Object>>)null);
  }

  @Test
  public void noEpicProvided() {
    expectedExceptionRule.expect(ReduxObservableException.class);
    expectedExceptionRule.expectMessage("You haven't provided any epics");

    CombinedEpic.combineEpics(new ArrayList<Epic<Object>>());
  }
}
