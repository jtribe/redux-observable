package au.com.jtribe.reduxobservable.exceptions;

/**
 * This gets thrown when something goes wrong when we're putting together a CombinedEpic for now,
 * but may be applicable elsewhere in the future.
 */
public class ReduxObservableException extends RuntimeException {
  private static final long serialVersionUID = -972406025773134476L;

  public ReduxObservableException(String s) {
    super(s);
  }

  public ReduxObservableException(String s, Throwable e) {
    super(s, e);
  }

  public ReduxObservableException(Throwable e) {
    super(e);
  }
}
