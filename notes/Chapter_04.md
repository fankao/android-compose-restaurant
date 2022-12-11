### 1. Suspending function

> A suspending function is a special function that can be paused (suspended) and resumed
> at some later point in time. This allows us to execute long-running jobs while the function
> is suspended and, finally, resume it when the work is complete

```
suspend fun storeUser(user: User) {
// blocking action
}
```

> Suspending functions can only be called from inside a coroutine or from inside another
> suspending function

```
fun saveDetails(user: User) {
    GlobalScope.launch(Dispatchers.IO) {
        startAnimation()
        database.storeUser(user)
        stopAnimation()
    }
}
```

### 2. Coroutine scopes

* **GlobalScope**: This allows the coroutines to live as long as the application is alive.
  In the previous example, we used this scope for simplicity, but **GlobalScope**
  should be avoided since the work launched within this coroutine scope is only
  canceled when the application has been destroyed. Using this scope in a component
  that has a narrower lifecycle than the application – such as an **Activity**
  component, might allow the coroutine to outlive that component's lifecycle and
  produce memory leaks.
* **lifecycleScope**: This scopes coroutines to the lifecycle of a **LifecycleOwner**
  instance such as an **Activity **component or a **Fragment **component. We can use
  the **lifecycleScope **scope defined in the Jetpack KTX extensions package:

  ```
  class UserFragment : Fragment() {
  ...
      fun saveDetails(user: User) {
          lifecycleScope.launch(Dispatchers.IO) {
              startAnimation()
              database.storeUser(user)
              stopAnimation()
          }
      }
  }

  ```
* **viewModelScope**: To scope our coroutines to live as long as the **ViewModel**
  component does, we can use the predefined **viewModelScope** scope:

  ```
  class UserViewModel: ViewModel() {
      fun saveDetails(user: User) {
          // do some work
          viewModelScope.launch(Dispatchers.IO) {
              database.storeUser(user)
          }
          // do some other work
      }
  }
  ```

  By launching coroutines within this context, we ensure that if the ViewModel component gets cleared, the coroutine scope will cancel its work – in other words, it will automatically cancel our coroutine
* **rememberCoroutineScope**: To scope a coroutine to the composition cycle of
  a composable function, we can use the predefined **rememberCoroutineScope**
  scope:

  ```
    @Composable
    fun UserComposable() {
      val scope = rememberCoroutineScope()
      LaunchedEffect(key1 = "save_user") {
       scope.launch(Dispatchers.IO) {
        viewModel.saveUser()
       }
     }
    }
  ```

> **Dispatchers
>
> A **CoroutineDispatcher** object allows us to configure what thread pool our work should
> be executed on. The point of coroutines is to help us move blocking work away from the
> main thread. So, somehow, we need to instruct the coroutines what threads to use for the
> work that we pass to them.

**Dispatchers.IO** is a dispatcher offered by the Coroutines API, but in addition to this,
coroutines offer other dispatchers too. Let's list the most notable dispatchers as follows:

* **Dispatchers.Main**: This dispatches work to the main thread on Android. It is
  ideal for light work (which doesn't block the UI) or actual UI function calls and
  interactions.
* **Dispatchers.IO**: This dispatches blocking work to a background thread
  pool that specializes in handling disk-heavy or network-heavy operations. This
  dispatcher should be specified for suspending work on local databases or executing
  network requests.
* **Dispatchers.Default**: This dispatches blocking work to a background thread
  pool that specializes in CPU-intensive tasks, such as sorting long lists, parsing
  JSON, and more.

### 3. Using predefined scopes as opposed to custom scopes

> Whenever you are launching a coroutine inside components such
> as Activity, Fragment, ViewModel, or even composable
> functions, remember that instead of creating and managing your own
> CoroutineScope object, you can use the predefined ones that take care of
> canceling coroutines automatically. By using predefined scopes, you can better
> avoid memory leaks as any suspending work is cancelled when needed
