package com.test.example.automation

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest : BaseTest() {

    @Rule
    @JvmField
    var mActivityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityTest() {
        val textView = onView(
            allOf(
                withText(R.string.first_fragment_label),
                isDisplayed()
            )
        )
        textView.check(matches(isDisplayed()))

        Assert.assertTrue(isDisplayed(withId(R.id.textview_first)))
        Assert.assertTrue(waitFor(R.id.textview_first))

        click(R.id.button_first)
        Assert.assertTrue(waitFor(R.id.textview_second))
    }
}
