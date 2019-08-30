package testUI;

import org.openqa.selenium.Cookie;
import testUI.Utils.TestUIException;
import testUI.elements.TestUI;
import testUI.elements.UIElement;

import java.util.Set;

import static testUI.AndroidTestUIDriver.*;
import static testUI.Configuration.*;
import static testUI.IOSTestUIDriver.*;
import static testUI.NetworkCalls.setNetworkCalls;
import static testUI.TestUIDriver.*;
import static testUI.UIUtils.putLog;
import static testUI.elements.TestUI.setScreenshotTaken;
import static testUI.elements.Element.setStep;

public class UIOpen {
    public static synchronized UIElement open() {
        setNetworkCalls();
        setScreenshotTaken(false);
        if (iOSTesting) {
            if (iOSAppPath.isEmpty() && bundleId.isEmpty() && getDesiredCapabilities() == null) {
                if (!androidAppPath.isEmpty() || (!appActivity.isEmpty() && !appPackage.isEmpty())) {
                    putLog("iOSTesting variable is set to true, but while all the Android variables are correctly set, the iOS ones aren't:"
                            + "\niOSAppPath = " + iOSAppPath
                            + "\n trying to start the Android app");
                    openApp(new TestUIConfiguration());
                } else {
                    throw new TestUIException("iOSAppPath or bundleId is mandatory field to run iOS apps, here are your iOS values:"
                            + "\niOSAppPath = " + iOSAppPath
                            + "\nbundelId = " + bundleId
                            + "\niOSDeviceName = " + iOSDeviceName
                            + "\niOSVersion = " + iOSVersion);
                }

            } else {
                openIOSApp(new TestUIConfiguration());
            }
        } else {
            if (androidAppPath.isEmpty() && (appActivity.isEmpty() && appPackage.isEmpty()) && getDesiredCapabilities() == null) {
                if (!iOSAppPath.isEmpty()) {
                    putLog("iOSTesting variable is set to false, but while all the iOS variables are correctly set, the android ones aren't:"
                            + "\nandroidAppPath = " + androidAppPath
                            + "\nappActivity = " + appActivity
                            + "\nappPackage = " + appPackage
                            + "\n trying to start the iOS app");
                    openIOSApp(new TestUIConfiguration());
                } else {
                    throw new TestUIException("androidAppPath or appActivity and appPackage are mandatory fields to run Android apps, but their values are:"
                            + "\nandroidAppPath = " + androidAppPath
                            + "\nappActivity = " + appActivity
                            + "\nappPackage = " + appPackage);
                }
            } else {
                openApp(new TestUIConfiguration());
            }
        }
        setStep(false);
        return TestUI.E("");
    }

    public static synchronized UIElement openNew() {
        setScreenshotTaken(false);
        if (iOSTesting) {
            if (iOSAppPath.isEmpty() && bundleId.isEmpty() && getDesiredCapabilities() == null) {
                if (!androidAppPath.isEmpty() && (!appActivity.isEmpty() && !appPackage.isEmpty())) {
                    putLog("iOSTesting variable is set to true, but while all the Android variables are correctly set, the iOS ones aren't:"
                            + "\niOSAppPath = " + iOSAppPath
                            + "\niOSDeviceName = " + iOSDeviceName
                            + "\niOSVersion = " + iOSVersion
                            + "\n trying to start the Android app");
                    openNewApp(new TestUIConfiguration());
                } else {
                    throw new TestUIException("iOSAppPath is mandatory fields to run iOS apps, here are your iOS values:"
                            + "\niOSAppPath = " + iOSAppPath
                            + "\niOSDeviceName = " + iOSDeviceName
                            + "\niOSVersion = " + iOSVersion);
                }
            } else {
                openNewIOSApp(new TestUIConfiguration());
            }
        } else {
            if (androidAppPath.isEmpty() && (appActivity.isEmpty() && appPackage.isEmpty()) && getDesiredCapabilities() == null) {
                if (!iOSAppPath.isEmpty()) {
                    putLog("iOSTesting variable is set to false, but while all the iOS variables are correctly set, the android ones aren't:"
                            + "\nandroidAppPath = " + androidAppPath
                            + "\nappActivity = " + appActivity
                            + "\nappPackage = " + appPackage
                            + "\n trying to start the iOS app");
                    openNewIOSApp(new TestUIConfiguration());
                } else {
                    throw new TestUIException("androidAppPath or appActivity and appPackage are mandatory fields to run Android apps, but their values are:"
                            + "\nandroidAppPath = " + androidAppPath
                            + "\nappActivity = " + appActivity
                            + "\nappPackage = " + appPackage);
                }
            } else {
                openNewApp(new TestUIConfiguration());
            }
        }
        setStep(false);
        return TestUI.E("");
    }

    public static synchronized UIElement open(String urlOrRelativeUrl) {
        setNetworkCalls();
        setScreenshotTaken(false);
        if (deviceTests && iOSTesting) {
            openIOSBrowser(urlOrRelativeUrl, new TestUIConfiguration());
        } else {
            openBrowser(urlOrRelativeUrl, new TestUIConfiguration());
        }
        setStep(false);
        return TestUI.E("");
    }

    public static UIElement navigate(String urlOrRelativeUrl) {
        navigateURL(urlOrRelativeUrl);
        return TestUI.E("");
    }

    public static UIElement openNew(String urlOrRelativeUrl) {
        setScreenshotTaken(false);
        if (deviceTests && iOSTesting) {
            openNewIOSBrowser(urlOrRelativeUrl, new TestUIConfiguration());
        } else {
            openNewBrowser(urlOrRelativeUrl, new TestUIConfiguration());
        }
        setStep(false);
        return TestUI.E("");
    }

    public static UIElement addCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        if (deviceTests) {
            getDriver().manage().addCookie(cookie);
        } else {
            getSelenideDriver().manage().addCookie(cookie);
        }
        return TestUI.E("");
    }

    public static Set<Cookie> getCookies() {
        if (deviceTests) {
            return getDriver().manage().getCookies();
        } else {
            return getSelenideDriver().manage().getCookies();
        }
    }
}
