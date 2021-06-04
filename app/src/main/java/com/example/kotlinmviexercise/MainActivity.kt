package com.example.kotlinmviexercise

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.keepers.instancekeeper.ExperimentalInstanceKeeperApi
import com.arkivanov.mvikotlin.keepers.instancekeeper.getInstanceKeeper
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.keepers.statekeeper.get
import com.arkivanov.mvikotlin.keepers.statekeeper.getSerializableStateKeeperRegistry
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

@ExperimentalInstanceKeeperApi
@ExperimentalStateKeeperApi
@InternalCoroutinesApi
class MainActivity : AppCompatActivity() {

    companion object {
        private var stateKeeper: StateKeeper<CalculatorStore.State>? = null
    }

    private lateinit var controller: CalculatorController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val instanceKeeper = getInstanceKeeper()
        val stateKeeperRegistry = getSerializableStateKeeperRegistry()

        if (stateKeeper == null) {
            stateKeeper = stateKeeperRegistry.get()
        }

        controller = DefaultCalculatorController(
            lifecycle = lifecycle.asMviLifecycle(),
            instanceKeeper,
            stateKeeper
        )

        GlobalScope.launch(Dispatchers.Main) {
            controller.flow.collect(object : FlowCollector<Long> {
                override suspend fun emit(value: Long) {
                    textView.text = value.toString()
                }
            })
        }

        incrementButton.setOnClickListener { controller.onIncrement() }
        decrementButton.setOnClickListener { controller.onDecrement() }
    }
}