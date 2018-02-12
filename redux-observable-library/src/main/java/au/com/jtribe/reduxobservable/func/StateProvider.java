package au.com.jtribe.reduxobservable.func;

/**
 * A function for providing the state when needed.
 * @param <S> The type of the State in our redux implementation.
 */
public interface StateProvider<S> {
  public S getState();
}
