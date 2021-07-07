package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@ExperimentalStateKeeperApi
internal class CalculatorStoreFactory(private val storeFactory: StoreFactory) {

    fun create(stateKeeper: StateKeeper<CalculatorStore.State>?): CalculatorStore =
        // Make sure the values of the generics are consistent with the store.
        object : CalculatorStore, Store<CalculatorStore.Intent, CalculatorStore.State, CalculatorStore.Label> by storeFactory.create(
            name = "CounterStore",
            initialState = CalculatorStore.State(),
            reducer = { intent ->
                when (intent) {
                is CalculatorStore.Intent.Increment -> CalculatorStore.State(intent.value + 1)
                is CalculatorStore.Intent.Decrement -> CalculatorStore.State(intent.value - 1)
                is CalculatorStore.Intent.Sum -> sum(1000000)
            } }
        ){
        }.also {
            stateKeeper?.register {
                // We can reset any transient state here
                it.state.copy(value = 0)
            }
        }

    private fun sum(n: Int) : CalculatorStore.State {
        val sum = (1L..n.toLong()).sum()
        return CalculatorStore.State(sum)
    }
}

