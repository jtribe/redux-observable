package au.com.jtribe.reduxobservable.redux;

import au.com.jtribe.reduxobservable.exceptions.ReduxObservableException;
import au.com.jtribe.reduxobservable.func.StateProvider;
import io.reactivex.Observable;
import java.util.Arrays;
import java.util.List;

/**
 * A convenience class for combining epics.
 */
public class CombinedEpic {

  private CombinedEpic() {
    throw new ReduxObservableException("No. You may not instantiate this class.");
  }

  /**
   * Takes a list of {@link Epic}s and combines them into one big Epic.
   * @param epics A collection of epics we want to combine.
   * @param <S> The type of State we have in our redux.
   * @return An Epic that combines all of the provided Epics together.
   */
  @SafeVarargs
  public static <S> Epic combineEpics(Epic<S>... epics) {
    return combineEpics(Arrays.asList(epics));
  }

  /**
   * Takes a list of {@link Epic}s and combines them into one big Epic.
   * @param epics A List of Epics we want to combine.
   * @param <S> The type of State we have in our redux.
   * @return An Epic that combines all of the provided Epics together.
   */
  public static <S> Epic<S> combineEpics(final List<Epic<S>> epics) {
    if (epics == null || epics.size() == 0) {
      throw new ReduxObservableException("You haven't provided any epics");
    } else {
      //noinspection unchecked
      return new Epic<S>() {
        @Override
        public Observable<Object> map(Observable<Object> actions, StateProvider<S> stateProvider) {
          return Observable.<Object>mergeArray(
              CombinedEpic.mapToObservables(epics, actions, stateProvider));
        }
      };
    }
  }

  private static <S> Observable[] mapToObservables(List<Epic<S>> epics, Observable<Object> actions,
      StateProvider<S> stateProvider) {
    Observable[] observables = new Observable[epics.size()];
    for (int i = 0; i < epics.size(); i++) {
      Epic<S> e = epics.get(i);
      Observable<Object> o = e.map(actions, stateProvider);

      observables[i] = o;
    }

    return observables;
  }
}
