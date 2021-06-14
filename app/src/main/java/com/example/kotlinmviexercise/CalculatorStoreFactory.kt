package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.core.utils.JvmSerializable
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@ExperimentalStateKeeperApi
internal class CalculatorStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainContext: CoroutineContext = Dispatchers.Main,
    private val calculationContext: CoroutineContext = Dispatchers.Default
) {

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

    private object reducer : Reducer<CalculatorStore.State, Result> {
        override fun CalculatorStore.State.reduce(result: Result): CalculatorStore.State {
            return when (result) {
                is Result.Value -> CalculatorStore.State(result.value)
            }
        }
    }

    private val bootstrapper = BootstrapperImpl(calculationContext)
    private val executorImpl = ExecutorImpl(calculationContext)
    private val executor: () -> Executor<CalculatorStore.Intent, Action, CalculatorStore.State, Result, CalculatorStore.Label> = {
        executorImpl
    }

    sealed class Action {
        class Sum(val n: Int): Action()
        class SetValue(val value: Long): Action()
    }

    private class BootstrapperImpl(private val calculationContext: CoroutineContext): SuspendBootstrapper<Action>() {
        override suspend fun bootstrap() {
            val sum = withContext(calculationContext) { (1L..1000000.toLong()).sum() }
            dispatch(Action.SetValue(sum))
        }
    }

    private class ExecutorImpl(private val calculationContext: CoroutineContext)
        : SuspendExecutor<CalculatorStore.Intent, Action, CalculatorStore.State, Result, CalculatorStore.Label>() {
        override suspend fun executeIntent(intent: CalculatorStore.Intent, getState: () -> CalculatorStore.State) =
            when (intent) {
                is CalculatorStore.Intent.Increment -> dispatch(Result.Value(getState().value + 1))
                is CalculatorStore.Intent.Decrement -> dispatch(Result.Value(getState().value - 1))
                is CalculatorStore.Intent.Sum -> sum(intent.n)
            }

        override suspend fun executeAction(action: Action, getState: () -> CalculatorStore.State) =
            when (action) {
                is Action.Sum -> sum(action.n)
                is Action.SetValue -> dispatch(Result.Value(action.value))
            }

        private suspend fun sum(n: Int) {
            val sum = withContext(calculationContext) { (1L..n.toLong()).sum() }
            dispatch(Result.Value(sum))
        }
    }

    sealed class Result : JvmSerializable {
        data class Value(val value: Long) : Result()
    }
}

