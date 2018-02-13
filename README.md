# redux-observable
An implementation of redux-observable for Java and Kotlin. See [the redux-observable website](https://redux-observable.js.org/) for details.

## How it works
To use redux-observable, you will need to provide your own implementation of an epic. An example epic may be as follows:

```java
    Epic<State> pingEpic = new Epic<State>() {
      @Override
      public Observable<Object> map(Observable<Object> actions, StateProvider<State> stateProvider) {
        return actions.ofType(PingAction.class)
                      .map(new Function<PingAction, Object>() {
                            @Override public Object apply(PingAction pingAction) throws Exception {
                              return new PongAction();
                            }
                          });
      }
    };
```

As you can see, an Epic requires a stream of actions. It also requires a means of getting at the state via a StateProvider implementation, which in most cases will be as simple as calling straight through to `store::getState` on your redux store or some other analogous method. 

It is possible to combine multiple Epics into one composite Epic via the `CombinedEpic.combineEpics(Epic<S>... epics)` helper method.

This library is engineered mainly for use with [redux-kotlin](https://github.com/pardom/redux-kotlin). There is an ```EpicMiddlewareFactory``` class provided by the library that will take an Epic and turn it into redux-kotlin compatible middleware. By plugging your epic into `EpicMiddlewareFactory.createMiddleware(Epic<S> epic)`, you will be given some `Middleware` that you can slot right into your redux.

If you wish to use the Epic implementation contained in this library in conjunction with some other redux, you will need to construct your own wrapper for your Epics to turn them into middleware. 

## To use this library:

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```groovy
dependencies {
        compile 'com.github.jtribe:redux-observable:0.1'
}
```
