package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnStartStop
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.view.BaseMviView
import com.arkivanov.mvikotlin.core.view.MviView
import com.arkivanov.mvikotlin.extensions.reaktive.events
import com.arkivanov.mvikotlin.extensions.reaktive.states
import com.arkivanov.mvikotlin.keepers.instancekeeper.ExperimentalInstanceKeeperApi
import com.arkivanov.mvikotlin.keepers.instancekeeper.InstanceKeeper
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import com.badoo.reaktive.observable.map
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
    private val stateToModel: CalculatorStore.State.() -> CalculatorView.Model =
        {
            CalculatorView.Model(
                value = value.toString()
            )
        }
    private val eventToIntent: CalculatorView.Event.() -> CalculatorStore.Intent =
        {
            when (this) {
                is CalculatorView.Event.IncrementClicked -> CalculatorStore.Intent.Increment
                is CalculatorView.Event.DecrementClicked -> CalculatorStore.Intent.Decrement
            }
        }
    private var binder: Binder? = null

    override val flow = MutableStateFlow(0L)

    init {
        // TODO: If I uncomment this code, a train wreck unfolds
//        lifecycle.doOnDestroy(store::dispose)
        // END TODO
        lifecycle.doOnStartStop(::onStart, ::onStop)

        storeFactoryInstance =
            LoggingStoreFactory(delegate = TimeTravelStoreFactory(fallback = DefaultStoreFactory))
        val calculatorStore = {
            CalculatorStoreFactory(storeFactoryInstance).create(stateKeeper)
        }
        store = instanceKeeper.getStore(calculatorStore)

        val view: MviView<CalculatorView.Model, CalculatorView.Event> = this
        binder =
            com.arkivanov.mvikotlin.extensions.reaktive.bind {
                store.states.map(stateToModel) bindTo view
                view.events.map(eventToIntent) bindTo store
            }
    }

    private fun onStart() {
        binder?.start()
    }

    private fun onStop() {
        binder?.stop()
    }

    override fun render(model: CalculatorView.Model) {
        flow.value = model.value.toLong()
    }

    override fun onIncrement() {
        dispatch(CalculatorView.Event.IncrementClicked)
    }

    override fun onDecrement() {
        dispatch(CalculatorView.Event.DecrementClicked)
    }

}
