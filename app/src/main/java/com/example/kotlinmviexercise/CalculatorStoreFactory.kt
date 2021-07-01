package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper

@ExperimentalStateKeeperApi
internal class CalculatorStoreFactory(private val storeFactory: StoreFactory) {

    private val initialValue = 0L

    fun create(stateKeeper: StateKeeper<CalculatorStore.State>?): CalculatorStore =
        // Make sure the values of the generics are consistent with the store.
        object : CalculatorStore, Store<CalculatorStore.Intent, CalculatorStore.State, CalculatorStore.State> by storeFactory.create(
            name = "CounterStore",
            initialState = CalculatorStore.State(initialValue),
            executorFactory = executor,
            reducer = { it }
        ){
        }.also {
            stateKeeper?.register {
                // We can reset any transient state here
                it.state.copy(value = initialValue)
            }
        }

    private val executorImpl = ExecutorImpl()
    private val executor: () -> Executor<CalculatorStore.Intent, Nothing, CalculatorStore.State, CalculatorStore.State, CalculatorStore.State> = {
        executorImpl
    }

    private class ExecutorImpl
        : SuspendExecutor<CalculatorStore.Intent, Nothing, CalculatorStore.State, CalculatorStore.State, CalculatorStore.State>() {
        override suspend fun executeIntent(intent: CalculatorStore.Intent, getState: () -> CalculatorStore.State) =
            when (intent) {
                is CalculatorStore.Intent.Increment -> {
                    val output = CalculatorStore.State(getState().value + 1)
                    broadcast(output)
                }
                is CalculatorStore.Intent.Decrement -> {
                    val output = CalculatorStore.State(getState().value - 1)
                    broadcast(output)
                }
            }

        private fun broadcast(output: CalculatorStore.State) {
            //Dispatch the `Result` to the [Reducer], which will `update the `State`
            dispatch(output)
            //Immediately send the `Label` to the [Store] for publication
            publish(output)
        }
    }
}

