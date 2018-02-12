package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.Observable;
import java.util.Arrays;
import java.util.List;

public class CombinedEpic<S> {

  private CombinedEpic() {
    throw new RuntimeException("YOU SHALL NOT INSTANTIATE!");
  }

  public static Epic combineEpics(Epic... epics) {
    return combineEpics(Arrays.asList(epics));
  }

  public static Epic combineEpics(List<Epic> epics) {
    if (epics == null || epics.size() == 0) {
      // todo: we've got no epics, how to handle that?
      return null;
    } else {
      return new Epic() {
        @Override
        public Observable<Object> map(Observable<Object> actions, StateProvider stateProvider) {
          return Observable.mergeArray(
              epics.stream() //
                  .map(e -> e.map(actions, stateProvider)) //
                  .<Observable<Object>>toArray(Observable[]::new));
        }
      };
    }
  }
}
