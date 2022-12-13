package com.test.example.automation

import android.view.View
import androidx.test.espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import androidx.test.espresso.util.TreeIterables
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeoutException

open class BaseTest {

    /**
     * Find a view with a view id
     * @param viewId The id of the view to matched
     */
    private fun findWithId(viewId: Int): ViewInteraction {
        return Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(viewId)
            )
        )
    }

    /**
     * Find a displayed view with a specific view id
     * @param viewId The id of the view to matched
     */
    private fun findDisplayed(viewId: Int): ViewInteraction {
        return Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(viewId),
                ViewMatchers.isDisplayed()
            )
        )
    }

    /**
     * Find a displayed copy and view with specific view id's
     * @param viewId The id of the view to matched
     * @param copy The string to be matched
     */
    private fun findWithIdAndCopy(viewId: Int, copy: String): ViewInteraction {
        return Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(copy),
                ViewMatchers.withId(viewId)
            )
        )
    }

    /**
     * Perform click action for a displayed specific view id
     * @param id The id of the view to matched
     */
    fun click(id: Int) {
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(id)
            )
        ).perform(ViewActions.click())
    }

    /**
     * Perform replaceText action that updates the text attribute
     * For a displayed specific view id and closes soft keyboard
     * @param text The text which will replace the current text
     * @param viewId The id of the view to matched
     */
    fun enter(text: String, viewId: Int) {
        findDisplayed(viewId).perform(
            ViewActions.replaceText(text),
            ViewActions.closeSoftKeyboard()
        )
    }

    /**
     * Perform replaceText action that updates the text attribute
     * For a displayed specific view id and closes soft keyboard
     * @param viewId The id of the view to matched
     */
    fun clearText(viewId: Int) {
        findDisplayed(viewId).perform(
            ViewActions.clearText(),
            ViewActions.closeSoftKeyboard()
        )
    }

    /**
     * Returns a {@link Boolean} for a {@link Matcher} view is displayed
     * @param matcher The matcher to be evaluated
     */
    fun isDisplayed(matcher: Matcher<View>): Boolean {
        val view = Espresso.onView(matcher)
        return tryViewAssertion(
            view,
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )
    }

    /**
     * Returns a {@link Boolean} for a view when view matches a {@link ViewAssertion}
     * @param view The ViewInteraction to be evaluated
     * @param assertion The ViewAssertion to be checked
     */
    private fun tryViewAssertion(view: ViewInteraction, assertion: ViewAssertion): Boolean {
        try {
            view.check(assertion)
        } catch (ex: Throwable) {
            when(ex) {
                is AssertionFailedError, is NoMatchingViewException -> {
                    return false
                }
                else -> throw ex
            }
        }

        return true
    }

    /**
     * Returns a {@link Boolean} after waiting for a specific view id
     * @param id The id of the view to be evaluated
     * @param millis The timeout in milliseconds
     */
    fun waitFor(id: Int, millis: Long = 5): Boolean {
        val viewMatcher = Matchers.allOf(ViewMatchers.withId(id), ViewMatchers.isDisplayed())

        try {
            Espresso.onView(ViewMatchers.isRoot()).perform(waitForMatch(viewMatcher, millis))
        } catch (e: PerformException) {
            print("View not displayed: $e")
            return false
        }

        return true
    }

    /**
     * Perform action of waiting for a specific Matcher<View>
     * @param matcher The specified matchers
     * @param millis The timeout for how long to wait for
     */
    private fun waitForMatch(matcher: Matcher<View>, millis: Long = 5): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for a specific view with id <$matcher> during $millis millis."
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis

                do {
                    for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (matcher.matches(child)) {
                            return
                        }
                    }

                    uiController.loopMainThreadForAtLeast(2)
                } while (System.currentTimeMillis() < endTime)

                // timeout happens
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(TimeoutException())
                    .build()
            }
        }
    }
}