package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.Observable;

public interface Epic {
  Observable<Object> map(Observable<Object> actions, StateProvider stateProvider);
}
