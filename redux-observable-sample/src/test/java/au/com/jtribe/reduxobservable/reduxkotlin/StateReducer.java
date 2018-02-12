package au.com.jtribe.reduxobservable.reduxkotlin;

import redux.api.Reducer;

public class StateReducer implements Reducer<Integer> {
  @Override public Integer reduce(Integer state, Object action) {
    return 1;
  }
}
