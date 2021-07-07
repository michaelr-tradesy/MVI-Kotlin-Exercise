package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnStartStop
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.extensions.reaktive.labels
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.arkivanov.mvikotlin.keepers.instancekeeper.ExperimentalInstanceKeeperApi
import com.arkivanov.mvikotlin.keepers.instancekeeper.InstanceKeeper
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import kotlinx.coroutines.flow.MutableStateFlow

interface CalculatorController {
    val flow: MutableStateFlow<Long>
    fun onIncrement()
    fun onDecrement()
}

@ExperimentalStateKeeperApi
@ExperimentalInstanceKeeperApi
class DefaultCalculatorController(
    lifecycle: Lifecycle,
    instanceKeeper: InstanceKeeper,
    private val stateKeeper: StateKeeper<CalculatorStore.State>?
) :
    CalculatorController, BaseMviView<CalculatorView.Model, CalculatorView.Event>() {

    private val store: CalculatorStore
    private val storeFactoryInstance: StoreFactory
    private var binder: Binder? = null

    override val flow = MutableStateFlow(0L)

    init {
        lifecycle.doOnStartStop(::onStart, ::onStop)

        storeFactoryInstance =
            LoggingStoreFactory(delegate = TimeTravelStoreFactory(fallback = DefaultStoreFactory))
        val calculatorStore = {
            CalculatorStoreFactory(storeFactoryInstance).create(stateKeeper)
        }
        store = instanceKeeper.getStore(calculatorStore)

        binder =
            com.arkivanov.mvikotlin.extensions.reaktive.bind {

                store.states.bindTo {
                    println("CounterStore: store.states(): New State=($it)...")
                    flow.value = it.value
                }
                store.labels.bindTo {
                    println("CounterStore: store.labels(): New Label=($it)...")
                }
            }
    }

    private fun onStart() {
        binder?.start()
    }

    private fun onStop() {
        binder?.stop()
    }

    override fun onIncrement() {
        store.accept(CalculatorStore.Intent.Increment(store.state.value))
    }

    override fun onDecrement() {
        store.accept(CalculatorStore.Intent.Decrement(store.state.value))
    }
}
