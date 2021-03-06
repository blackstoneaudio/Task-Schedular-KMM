# Task Scheduler KMM
This is a simple description system for defining tasks in a KMM app that need to run either on Boot or at fixed intervals thought the app lifecycle.

## How to Add Task Scheduler to Your App
Add the repository to your project's `build.gradle.kts`. I'm using kotlin for my gradle syntax, but groovy
will be pretty similar if that's what you use
```kotlin 
repositories {
    ...
    maven("https://repos.awhb.dev")
    ...
}
```

add following in your commonMain source set
```kotlin
sourceSets {
  val commonMain by getting {
    dependencies {
      ...
      implementation("com.blackstone:TaskScheduler:0.1.0")
        ...
    }
  }
}
  ...
```




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

This was a fun project to learn a bit about more advanced Kotlin DSL generation. I am using a very similar pattern as [Ktor](https://ktor.io/docs/client.html) for this DSL and was very inspired by their `install()` builder pattern. Feel free to send PRs if you would like to add anything to this. The code is very simple at the moment. 
