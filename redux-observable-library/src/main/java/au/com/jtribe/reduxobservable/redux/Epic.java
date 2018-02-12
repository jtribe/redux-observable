package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.Observable;

public interface Epic<S> {
  /**
   * Maps a stream of Objects (actions) into another stream of actions. This can include other side-effects, such as making API calls and so on.
   * @param actions An Observable stream that serves up actions. It should be unfiltered at this stage; we do the filtering inside the Epic.
   * @param stateProvider A function which returns the State of our redux implementation.
   * @return A transformed stream of Objects -- actions we want our Epic to emit.
   */
  Observable<Object> map(Observable<Object> actions, StateProvider<S> stateProvider);
}
