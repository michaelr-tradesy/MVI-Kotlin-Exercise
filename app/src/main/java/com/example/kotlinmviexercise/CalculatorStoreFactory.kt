package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.Executor
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@ExperimentalStateKeeperApi
internal class CalculatorStoreFactory(private val storeFactory: StoreFactory,
                                      calculationContext: CoroutineContext = Dispatchers.Default) {

    fun create(stateKeeper: StateKeeper<CalculatorStore.State>?): CalculatorStore =
        // Make sure the values of the generics are consistent with the store.
        object : CalculatorStore, Store<CalculatorStore.Intent, CalculatorStore.State, CalculatorStore.Label> by storeFactory.create(
            name = "CounterStore",
            initialState = CalculatorStore.State(),
            bootstrapper = bootstrapper,
            executorFactory = executor,
            reducer = reducer
        ){
        }.also {
            stateKeeper?.register {
                // We can reset any transient state here
                it.state.copy(value = 0)
            }
        }

    private object reducer : Reducer<CalculatorStore.State, CalculatorStore.Result> {
        override fun CalculatorStore.State.reduce(result: CalculatorStore.Result): CalculatorStore.State {
            return when {
                else -> CalculatorStore.State(result.value)
            }
        }
    }

    private val bootstrapper = BootstrapperImpl(calculationContext)
    private val executorImpl = ExecutorImpl(calculationContext)
    private val executor: () -> Executor<CalculatorStore.Intent, CalculatorStore.Action, CalculatorStore.State, CalculatorStore.Result, CalculatorStore.Label> = {
        executorImpl
    }

    private class BootstrapperImpl(private val calculationContext: CoroutineContext): SuspendBootstrapper<CalculatorStore.Action>() {
        override suspend fun bootstrap() {
            val sum = withContext(calculationContext) { (1L..1000000.toLong()).sum() }
            dispatch(CalculatorStore.Action.SetValue(sum))
        }
    }

    private class ExecutorImpl(private val calculationContext: CoroutineContext)
        : SuspendExecutor<CalculatorStore.Intent, CalculatorStore.Action, CalculatorStore.State, CalculatorStore.Result, CalculatorStore.Label>() {
        override suspend fun executeIntent(intent: CalculatorStore.Intent, getState: () -> CalculatorStore.State) =
            when (intent) {
                is CalculatorStore.Intent.Increment -> dispatch(CalculatorStore.Result(getState().value + 1))
                is CalculatorStore.Intent.Decrement -> dispatch(CalculatorStore.Result(getState().value - 1))
                is CalculatorStore.Intent.Sum -> sum(1000000)
            }

        override suspend fun executeAction(action: CalculatorStore.Action, getState: () -> CalculatorStore.State) {
            publish(CalculatorStore.Label(CalculatorStore.LabelType.InProgress))

            when (action) {
                is CalculatorStore.Action.Sum -> sum(action.n)
                is CalculatorStore.Action.SetValue -> dispatch(CalculatorStore.Result(action.value))
            }

            publish(CalculatorStore.Label(CalculatorStore.LabelType.Success))
        }

        private suspend fun sum(n: Int) {
            val sum = withContext(calculationContext) { (1L..n.toLong()).sum() }
            dispatch(CalculatorStore.Result(sum))
        }

        private fun broadcast(output: CalculatorStore.Result) {
            //Dispatch the `Result` to the [Reducer], which will `update the `State`
            dispatch(output)
        }

        private fun publishLabel(output: CalculatorStore.Label) {
            //Immediately send the `Label` to the [Store] for publication
            publish(output)
        }
    }
}

