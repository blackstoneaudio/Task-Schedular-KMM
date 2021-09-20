package com.blackstone.library

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * ref: https://gist.github.com/gmk57/67591e0c878cedc2a318c10b9d9f4c0c
 * ref: https://github.com/Kotlin/kotlinx.coroutines/issues/1186
 * Coroutine-based solution for delayed and periodic work. May fire once (if [interval] omitted)
 * or periodically ([startDelay] defaults to [interval] in this case), replacing both
 * `Observable.timer()` & `Observable.interval()` from RxJava.
 *
 * In contrast to RxJava, intervals are calculated since previous run completion; this is more
 * convenient for potentially long work (prevents overlapping) and does not suffer from queueing
 * multiple invocations in Doze mode on Android.
 *
 * Dispatcher is inherited from scope, may be overridden via [context] parameter.
 *
 * Inspired by [https://github.com/Kotlin/kotlinx.coroutines/issues/1186#issue-443483801]
 */

@OptIn(ExperimentalTime::class)
fun CoroutineScope.timer(
    interval: Duration = Duration.ZERO,
    startDelay: Duration = interval,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job = launch(context) {
    delay(startDelay)
    do {
        block()
        delay(interval)
    } while (interval > Duration.ZERO)
}

/** Variant of [timer] with intervals (re)calculated since the beginning (like in RxJava), for cases
 *  where accumulating time shift due to [delay] non-exactness & time spent in [block] is undesirable */
// TODO this one could be useful for the hybrid epub reader.
@ExperimentalTime
fun CoroutineScope.timerExact(
    interval: Duration = Duration.ZERO,
    startDelay: Duration = interval,
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend () -> Unit
): Job = launch(context) {
    val startTime = TimeSource.Monotonic.markNow()
    var count: Long = 0
    delay(startDelay)
    do {
        block()
        // Long to Double conversion is generally lossy, but values up to 2^53 (285 million years
        // for 1-second intervals) will be represented exactly, see https://stackoverflow.com/a/1848762
        val nextTime = startTime + startDelay + interval * (++count).toDouble()
        delay(nextTime.remaining())
    } while (interval > Duration.ZERO)
}

/** Returns the amount of time remaining until this mark (opposite of [TimeMark.elapsedNow]) */
@ExperimentalTime
fun TimeMark.remaining(): Duration = -elapsedNow()
