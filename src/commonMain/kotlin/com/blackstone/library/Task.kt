package com.blackstone.library

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


typealias RunWhen = () -> Boolean
typealias ExecutableTask = suspend CoroutineScope.() -> Unit

interface ScheduleFeature<out TConfig : Any, TFeature : Any> {
    /**
     * Builds a [TFeature] by calling the [block] with a [TConfig] config instance as receiver.
     */
    public fun prepare(block: TConfig.() -> Unit = {}): TFeature

    /**
     * Installs the [feature] class for a [TaskBuilder] defined at [scope].
     */
    public fun install(feature: TFeature, scope: TaskBuilder)
}


sealed interface TaskType


object EmptyTask : TaskType

private fun emptyTask(): EmptyTask {
    return EmptyTask
}


@ExperimentalTime
class ScheduledTask internal constructor(val startTime: Duration, val runEvery: Duration, val runWhen: RunWhen) :
    TaskType {

    class Config {
        var startTime: Duration = Duration.ZERO
        var runEvery: Duration = Duration.ZERO // if this is not set then it will only run once
        var runWhen: RunWhen = { true } // defaults to true so it will always run on the set interval
    }

    companion object Feature : ScheduleFeature<Config, ScheduledTask> {

        override fun prepare(block: Config.() -> Unit): ScheduledTask {
            val config = Config().apply(block)

            return ScheduledTask(config.startTime, config.runEvery, config.runWhen)
        }

        override fun install(feature: ScheduledTask, scope: TaskBuilder) {
            scope.apply {
                taskType = feature
            }
        }
    }
}

class OnAppBoot internal constructor(val runWhen: RunWhen) : TaskType {

    class Config {
        var runWhen: RunWhen = { false }
    }

    companion object Feature : ScheduleFeature<Config, OnAppBoot> {
        override fun prepare(block: Config.() -> Unit): OnAppBoot {
            val config = Config().apply(block)

            return OnAppBoot(config.runWhen)
        }

        override fun install(feature: OnAppBoot, scope: TaskBuilder) {
            scope.apply {
                taskType = feature
            }
        }
    }
}


data class Task(
    val coroutinesScope: CoroutineScope,
    val task: ExecutableTask,
    val priority: Int,
    val taskType: TaskType
)

@ExperimentalTime
class Scheduler private constructor() {
    private val scheduledTasks: MutableList<Task> = mutableListOf()

    fun registerTask(task: Task) {
        scheduledTasks.add(task)
        setupTask(task)
    }

    private fun setupTask(task: Task) {
        when (task.taskType) {
            EmptyTask -> {
            /* do nothing */
            }
            is OnAppBoot -> {
                task.coroutinesScope.launch {
                    if (task.taskType.runWhen()) task.task.invoke(this)
                }
            }
            is ScheduledTask -> {
                task.coroutinesScope.timer(task.taskType.runEvery, task.taskType.startTime) {
                    if (task.taskType.runWhen()) task.task(this)
                }
            }
        }
    }

    companion object {
        val shared = Scheduler()
    }

}


@TaskSchedulerDsl
class TaskBuilder {
    var coroutinesScope = MainScope()
    var task: ExecutableTask = {}
    var priority: Int = 0 // low number low priority. max is 10
    var taskType: TaskType = emptyTask() // this has no functional operation

    fun <TBuilder : Any, TFeature : Any> use(
        feature: ScheduleFeature<TBuilder, TFeature>,
        configure: TBuilder.() -> Unit = {}
    ) {
        check(taskType is EmptyTask) { "You can only assign one task to a register {} at a time. Make another call to register if you'd like an additional task type" }
        val featureData = feature.prepare(configure)
        feature.install(featureData, this)
        check(taskType !is EmptyTask) { "You don't have a valid task. Empty task does nothing" }
    }

    fun build(): Task {
        require(priority <= 10) { "Priority must be 10 or less" }
        return Task(coroutinesScope, task, priority, taskType)
    }
}


@OptIn(ExperimentalTime::class)
@TaskSchedulerDsl
fun register(block: TaskBuilder.() -> Unit) {
    val task = TaskBuilder().apply(block).build()

    Scheduler.shared.registerTask(task)
}

/**
 * Dsl marker for [Task] dsl.
 */
@DslMarker
public annotation class TaskSchedulerDsl