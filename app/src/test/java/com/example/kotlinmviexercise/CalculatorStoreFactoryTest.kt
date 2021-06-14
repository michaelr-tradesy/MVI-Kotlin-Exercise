package com.example.kotlinmviexercise

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@ExperimentalStateKeeperApi
class CalculatorStoreTest {

    private val coroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun before() {
        isAssertOnMainThreadEnabled = false
    }

    @After
    fun after() {
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun `When created THEN initial value calculated`() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(coroutineDispatcher)
        val store = store()
        assertEquals(INITIAL_VALUE, store.state.value)
        Dispatchers.resetMain()
        coroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `When intent increment THEN value incremented by one`() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(coroutineDispatcher)

        val store = store()

        store.accept(CalculatorStore.Intent.Increment)

        assertEquals(INITIAL_VALUE + 1, store.state.value)
        Dispatchers.resetMain()
        coroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `When intent decrement THEN value decremented by one`() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(coroutineDispatcher)

        val store = store()

        store.accept(CalculatorStore.Intent.Decrement)

        assertEquals(INITIAL_VALUE - 1, store.state.value)
        Dispatchers.resetMain()
        coroutineDispatcher.cleanupTestCoroutines()
        isAssertOnMainThreadEnabled = true
    }

    @Test
    fun `When intent sum 5 THEN value is 15`() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(coroutineDispatcher)

        val store = store()

        store.accept(CalculatorStore.Intent.Sum(n = 5))

        assertEquals(15, store.state.value)
        Dispatchers.resetMain()
        coroutineDispatcher.cleanupTestCoroutines()
    }

    private fun store(): CalculatorStore =
        CalculatorStoreFactory(
            storeFactory = DefaultStoreFactory,
            mainContext = coroutineDispatcher,
            calculationContext = coroutineDispatcher
        ).create(
            stateKeeper = null
        )

    private companion object {
        private const val INITIAL_VALUE = 500000500000
    }
}