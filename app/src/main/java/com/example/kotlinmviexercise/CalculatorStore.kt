package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.utils.JvmSerializable

// Following the suggested implementation as specified by arkivanov
// https://github.com/arkivanov/MVIKotlin/blob/master/mvikotlin/src/commonMain/kotlin/com/arkivanov/mvikotlin/core/store/Store.kt
// https://arkivanov.github.io/MVIKotlin/
interface CalculatorStore : Store<CalculatorStore.Intent, CalculatorStore.State, CalculatorStore.Label> {

    sealed class Action {
        class Sum(val n: Int): Action()
        class SetValue(val value: Long): Action()
    }

    sealed class Intent: JvmSerializable {
        data class Increment(val value: Long = 0L) : Intent()
        data class Decrement (val value: Long = 0L): Intent()
        object Sum : Intent()
    }

    data class Result(
        val value: Long = 0L
    ): JvmSerializable

    data class State(
        val value: Long = 0L
    ): JvmSerializable

    data class Label(
        val value: LabelType = LabelType.Idle
    ): JvmSerializable

    enum class LabelType {
        Idle, InProgress, Error, Success
    }
}