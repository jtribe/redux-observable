package au.com.jtribe.reduxobservable.reduxkotlin;

import redux.api.Reducer;

public class StateReducer implements Reducer<State> {
  @Override public State reduce(State state, Object action) {
    if (action instanceof BeepAction) {
      return new State(state.i + 1);
    }
    else if (action instanceof BoopAction) {
      return new State(state.i - 2);
    }
    return state;
  }
}
