package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import redux.api.Dispatcher;
import redux.api.Store;
import redux.api.enhancer.Middleware;

/**
 * This is a sample implementation of Middleware that will work with the redux-kotlin library
 * available at https://github.com/pardom/redux-kotlin. It's a relatively straightforward
 * implementation of redux and has a nice way of folding in all of the middleware that a lot of
 * these libraries lack, so we'll just use it for this example.
 *
 * The createMiddleware() method will generate a Middleware instance that will subscribe to an epic
 * and forward on any events spat out by the epic.
 */

public class EpicMiddlewareFactory {

  private EpicMiddlewareFactory() {
    throw new RuntimeException(
        "Instantiating an instance of this class, no matter how much you might like to, is not recommended");
  }

  public static <S> Middleware<S> createMiddleware(final Epic<S> e) {
    return new Middleware<S>() {
      boolean subscribed = false;
      PublishSubject<Object> actions = PublishSubject.create();

      @Override public Object dispatch(final Store<S> store, Dispatcher next, Object action) {
        if (!subscribed) {
          e.map(actions, new StateProvider<S>() {
            @Override public S getState() {
              return store.getState();
            }
          }).subscribe(new Consumer<Object>() {
            @Override public void accept(Object action) throws Exception {
              store.dispatch(action);
            }
          });
          subscribed = true;
        }
        Object result = next.dispatch(action);
        actions.onNext(action);
        return result;
      }
    };
  }
}
