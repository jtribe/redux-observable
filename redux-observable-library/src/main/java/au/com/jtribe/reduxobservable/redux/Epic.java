package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.Observable;

public interface Epic<S> {
  Observable<Object> map(Observable<Object> actions, StateProvider<S> stateProvider);
}
