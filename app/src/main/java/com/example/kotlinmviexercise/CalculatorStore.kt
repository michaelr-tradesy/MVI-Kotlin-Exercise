package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.utils.JvmSerializable

// Following the suggested implementation as specified by arkivanov
// https://github.com/arkivanov/MVIKotlin/blob/master/mvikotlin/src/commonMain/kotlin/com/arkivanov/mvikotlin/core/store/Store.kt
// https://arkivanov.github.io/MVIKotlin/
interface CalculatorStore : Store<CalculatorStore.Intent, CalculatorStore.State, CalculatorStore.Label> {

    sealed class Intent: JvmSerializable {
        object Increment : Intent()
        object Decrement : Intent()
        data class Sum(val n: Int): Intent()
    }

    data class State(
        val value: Long = 0L
    ): JvmSerializable

    sealed class Label : JvmSerializable {
        data class Increment(val item: CalculatorStore) : Label()
        data class Decrement(val item: CalculatorStore) : Label()
    }
}