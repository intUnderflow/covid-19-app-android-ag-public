package uk.nhs.nhsx.covid19.android.app.onboarding

import com.schibsted.spain.barista.interaction.BaristaSleepInteractions.sleep
import org.junit.Test
import uk.nhs.nhsx.covid19.android.app.onboarding.postcode.PostCodeActivity
import uk.nhs.nhsx.covid19.android.app.report.Reporter
import uk.nhs.nhsx.covid19.android.app.report.notReported
import uk.nhs.nhsx.covid19.android.app.report.reporter
import uk.nhs.nhsx.covid19.android.app.testhelpers.retry.RetryFlakyTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.base.EspressoTest
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.DataAndPrivacyRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.PermissionRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.PostCodeRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.StatusRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.WelcomeRobot
import uk.nhs.nhsx.covid19.android.app.testhelpers.robots.edgecases.AgeRestrictionRobot

class OnboardingScenarioTest : EspressoTest() {

    private val welcomeRobot = WelcomeRobot()
    private val postCodeRobot = PostCodeRobot()
    private val permissionRobot = PermissionRobot()
    private val statusRobot = StatusRobot()
    private val dataAndPrivacyRobot = DataAndPrivacyRobot()
    private val ageRestrictionRobot = AgeRestrictionRobot()

    @Test
    fun onboardingSuccessful_navigateToStatusScreen() = reporter(
        "Onboarding",
        "Happy path",
        "Complete onboarding flow",
        Reporter.Kind.FLOW
    ) {
        startTestActivity<WelcomeActivity>()

        welcomeRobot.checkActivityIsDisplayed()

        sleep(100)

        step(
            "Start",
            "The user is presented a screen with information on what this app can do. The user continues."
        )

        welcomeRobot.clickConfirmOnboarding()

        welcomeRobot.checkAgeConfirmationDialogIsDisplayed()

        step(
            "Confirm age",
            "The user is asked to confirm they are older than 16 years. The user confirms to be older than 16."
        )

        welcomeRobot.clickConfirmAgePositive()

        waitFor { dataAndPrivacyRobot.checkActivityIsDisplayed() }

        step(
            "Data and privacy",
            "The user is presented a screen with information on data and privacy notes. The user continues."
        )

        dataAndPrivacyRobot.clickConfirmOnboarding()

        postCodeRobot.checkActivityIsDisplayed()

        step(
            "Enter postcode",
            "The user is asked to enter their partial postcode before they can proceed."
        )

        postCodeRobot.enterPostCode("SE1")

        step(
            "Postcode entered",
            "The user enters a valid postcode and continues."
        )

        waitFor { postCodeRobot.checkContinueButtonIsDisplayed() }

        postCodeRobot.clickContinue()

        permissionRobot.checkActivityIsDisplayed()

        step(
            "Permissions",
            "The user is presented with information on which permissions are necessary for the app. The user continues."
        )

        permissionRobot.clickEnablePermissions()

        statusRobot.checkActivityIsDisplayed()

        step(
            "Home screen",
            "The user is presented with the home screen."
        )
    }

    @RetryFlakyTest
    @Test
    fun onboardingAgeConfirmationNegative_showAgeRestrictionScreen() = reporter(
        "Onboarding",
        "User not 16+",
        "User is not 16+ and can not proceed.",
        Reporter.Kind.FLOW
    ) {
        startTestActivity<WelcomeActivity>()

        welcomeRobot.checkActivityIsDisplayed()

        step(
            "Start",
            "The user is presented a screen with information on what this app can do. The user continues."
        )

        welcomeRobot.clickConfirmOnboarding()

        sleep(100)

        welcomeRobot.checkAgeConfirmationDialogIsDisplayed()

        step(
            "Confirm age",
            "The user is asked to confirm they are older than 16 years. The user confirms to be older than 16."
        )

        welcomeRobot.clickConfirmAgeNegative()

        ageRestrictionRobot.checkActivityIsDisplayed()

        step(
            "User under 16",
            "The user is shown a screen that informs them they are not allowed to use the app."
        )
    }

    @Test
    fun onboardingFailedBecauseInvalidPostcodeEntered_showInvalidPostcodeError() = reporter(
        "Onboarding",
        "Invalid postcode",
        "User enters invalid postcode",
        Reporter.Kind.SCREEN
    ) {
        startTestActivity<PostCodeActivity>()

        postCodeRobot.checkActivityIsDisplayed()

        step(
            "Start",
            "The user is asked to enter their partial postcode before they " +
                "can proceed."
        )

        postCodeRobot.enterPostCode("INV")

        step(
            "Postcode entered",
            "The user enters an invalid postcode and continues."
        )

        waitFor { postCodeRobot.checkContinueButtonIsDisplayed() }

        postCodeRobot.clickContinue()

        postCodeRobot.checkErrorTitleIsDisplayed()

        step(
            "Postcode invalid",
            "The user is shown an error that the postcode they entered is invalid."
        )
    }

    @Test
    fun validPostcodeEnteredAndContinueClicked_navigateToPermissionScreen() = notReported {
        startTestActivity<PostCodeActivity>()

        postCodeRobot.checkActivityIsDisplayed()

        postCodeRobot.enterPostCode("AL1")

        waitFor { postCodeRobot.checkContinueButtonIsDisplayed() }

        postCodeRobot.clickContinue()

        permissionRobot.checkActivityIsDisplayed()
    }

    @Test
    fun grantExposureNotificationPermissions_shouldEventuallyShowStatusScreen() = notReported {
        startTestActivity<PermissionActivity>()

        permissionRobot.checkActivityIsDisplayed()

        permissionRobot.clickEnablePermissions()

        statusRobot.checkActivityIsDisplayed()
    }

    @Test
    fun userEntersNotSupportedPostCode_shouldSeeErrorMessage() = reporter(
        "Onboarding",
        "Unsupported postcode",
        "Show error when user enters not supported code",
        Reporter.Kind.SCREEN
    ) {
        startTestActivity<PostCodeActivity>()

        postCodeRobot.checkActivityIsDisplayed()

        step(
            "Start",
            "The user is asked to enter their partial postcode before they can proceed."
        )

        postCodeRobot.enterPostCode("BT1")

        step(
            "Postcode entered",
            "The user enters a postcode that is not supported and clicks to continue."
        )

        waitFor { postCodeRobot.checkContinueButtonIsDisplayed() }

        postCodeRobot.clickContinue()

        waitFor { postCodeRobot.checkErrorContainerForNotSupportedPostCodeIsDisplayed() }

        step(
            "Postcode not supported",
            "The user is shown an error that the postcode they entered is not supported."
        )
    }
}
