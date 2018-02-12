package au.com.jtribe.reduxobservable.reduxkotlin;

import au.com.jtribe.reduxobservable.redux.Epic;
import io.reactivex.subjects.PublishSubject;
import redux.api.Dispatcher;
import redux.api.Store;
import redux.api.enhancer.Middleware;

/**
 * This is a sample implementation of Middleware that will work with the redux-kotlin library
 * available at https://github.com/pardom/redux-kotlin. It will generate a Middleware instance that
 * will subscribe to an epic and forward on any events spat out by the epic.
 *
 * It's a relatively straightforward implementation of redux and has a nice way of folding in all of
 * the middleware that a lot of these libraries lack, so we'll just use it for this example.
 */

public class EpicMiddlewareFactory<S> {
  public static <S> Middleware<S> createMiddleware(Epic e) {
    return new Middleware<S>() {
      boolean subscribed = false;
      PublishSubject<Object> actions = PublishSubject.create();

      @Override public Object dispatch(Store<S> store, Dispatcher next, Object action) {
        System.out.println("Received a " + action);
        if (!subscribed) {
          System.out.println("Subscribing...");
          e.map(actions, store::getState).subscribe(store::dispatch);
          subscribed = true;
        }
        Object result = next.dispatch(action);
        actions.onNext(action);
        return result;
      }
    };
  }
}
