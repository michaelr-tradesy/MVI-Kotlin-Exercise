package com.example.kotlinmviexercise


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.arkivanov.mvikotlin.keepers.instancekeeper.ExperimentalInstanceKeeperApi
import com.arkivanov.mvikotlin.keepers.statekeeper.ExperimentalStateKeeperApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalStateKeeperApi
@ExperimentalInstanceKeeperApi
@InternalCoroutinesApi
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private val initialValue = "500000500000"
    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        val textView = onView(
allOf(withId(R.id.textView), withText(initialValue),
withParent(withParent(withId(android.R.id.content))),
isDisplayed()))
        textView.check(matches(withText(initialValue)))

        val appCompatButton = onView(
allOf(withId(R.id.incrementButton), withText("Increment"),
childAtPosition(
childAtPosition(
withId(android.R.id.content),
0),
1),
isDisplayed()))
        appCompatButton.perform(click())

        val textView2 = onView(
allOf(withId(R.id.textView), withText("500000500001"),
withParent(withParent(withId(android.R.id.content))),
isDisplayed()))
        textView2.check(matches(withText("500000500001")))

        val appCompatButton2 = onView(
allOf(withId(R.id.decrementButton), withText("Decrement"),
childAtPosition(
childAtPosition(
withId(android.R.id.content),
0),
2),
isDisplayed()))
        appCompatButton2.perform(click())

        val textView3 = onView(
allOf(withId(R.id.textView), withText(initialValue),
withParent(withParent(withId(android.R.id.content))),
isDisplayed()))
        textView3.check(matches(withText(initialValue)))
        }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
    }
