# Task Schedular KMM
This is a simple description system for defining tasks in a KMM app that need to run either on Boot or at fixed intervals thought the app lifecycle.


## Examples of how to use it:
```kotlin
  register {
      coroutinesScope = MainScope()
      task = {
          // do something here when the app boots and run when is true
      }
      use(OnAppBoot) {
          runWhen = {
              true
          }
      }
  }
```


Another example: 
```kotlin 
        register {
            coroutinesScope = MainScope
            task = {
                // run this code every 10 minutes 
            }
            use(ScheduledTask) {
                startTime = Duration.ZERO // no start delay
                runEvery = Duration.minutes(10)
            }
        }
```
