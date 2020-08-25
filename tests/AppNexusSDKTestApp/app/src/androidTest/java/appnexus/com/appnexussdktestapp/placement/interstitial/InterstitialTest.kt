/*
 *    Copyright 2019 APPNEXUS INC
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package appnexus.com.appnexussdktestapp.placement.interstitial


import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import appnexus.com.appnexussdktestapp.InterstitialActivity
import com.appnexus.opensdk.AdActivity
import com.microsoft.appcenter.espresso.Factory
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class InterstitialTest {

    @get:Rule
    var reportHelper = Factory.getReportHelper()

    @Rule
    @JvmField
    var mActivityTestRule = IntentsTestRule(InterstitialActivity::class.java, false, false)

    lateinit var interstitialActivity: InterstitialActivity

    @Before
    fun setup() {
        IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
        IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
        var intent = Intent()
        mActivityTestRule.launchActivity(intent)
        interstitialActivity = mActivityTestRule.activity
        IdlingRegistry.getInstance().register(interstitialActivity.idlingResource)
    }

    @After
    fun destroy() {
        IdlingRegistry.getInstance().unregister(interstitialActivity.idlingResource)
        reportHelper.label("Stopping App")
    }

    /*
    * Sanity Test for the Interstitial Ad along with autoDismissDelay of 5 sec
    * */
    @Test
    fun interstitialActivityTest() {

        interstitialActivity.triggerAdLoad("17058950", autoDismiss = -1, closeButtonDelay = 2, creativeId = 166843001)

        //check if the triggered intent is pointing to the AdActivity
        intended(IntentMatchers.hasComponent(AdActivity::class.java.name))

        val closeButtonId = android.R.id.closeButton

        Espresso.onView(ViewMatchers.withId(closeButtonId)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(closeButtonId)).perform(ViewActions.click())

        assertTrue(interstitialActivity.isAdCollapsed)
    }
    
    // BG Testing

    /*
    * Sanity Test for the Interstitial Ad along with autoDismissDelay of 5 sec
    * */
    @Test
    fun interstitialActivityBGTest() {

        interstitialActivity.triggerAdLoad("17058950", autoDismiss = -1, closeButtonDelay = 2, creativeId = 166843001, bgTask = true)

        //check if the triggered intent is pointing to the AdActivity
        intended(IntentMatchers.hasComponent(AdActivity::class.java.name))

        val closeButtonId = android.R.id.closeButton

        Espresso.onView(ViewMatchers.withId(closeButtonId)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(closeButtonId)).perform(ViewActions.click())

        assertTrue(interstitialActivity.isAdCollapsed)
    }
}
