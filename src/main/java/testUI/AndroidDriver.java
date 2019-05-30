package testUI;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.remote.DesiredCapabilities;

import static testUI.Configuration.*;
import static testUI.TestUIDriver.*;
import static testUI.TestUIServer.attachShutDownHookStopEmulator;
import static testUI.TestUIServer.startServerAndDevice;
import static testUI.UIUtils.*;

public class AndroidDriver {

    // ANDROID APP AND BROWSER SUPPORT

    public static void openApp() {
        deviceTests = true;
        iOSTesting = false;
        if (getServices().size() == 0 || !getServices().get(0).isRunning() && desiredCapabilities == null) {
            startServerAndDevice();
            DesiredCapabilities cap = setAppAndroidCapabilities();
            startFirstDriver(cap);
            if (!emulatorName.isEmpty()) {
                setDevice(getDriver().getCapabilities().asMap().get("deviceUDID").toString(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
                attachShutDownHookStopEmulator(getServices(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
            }
            putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
        } else {
            driver = 1;
            DesiredCapabilities cap = setAppAndroidCapabilities();
            if (appiumUrl.isEmpty()) {
                putAllureParameter("Using Appium port", usePort.get(0));
            } else {
                putAllureParameter("Using Appium url", appiumUrl);
            }
            startFirstDriver(cap);
            putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
        }
        emulatorName = "";
    }

    public static void openNewApp() {
        deviceTests = true;
        iOSTesting = false;
        startServerAndDevice();
        DesiredCapabilities cap = setAppAndroidCapabilities();
        startDriver(cap);
        if (!emulatorName.isEmpty()) {
            setDevice(getDriver().getCapabilities().asMap().get("deviceUDID").toString(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
            attachShutDownHookStopEmulator(getServices(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
        }
        putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
        emulatorName = "";
    }

    public static void openBrowser(String urlOrRelativeUrl) {
        iOSTesting = false;
        if (deviceTests) {
            urlOrRelativeUrl = baseUrl + urlOrRelativeUrl;
            if ((getServices().size() == 0 || !getServices().get(0).isRunning()) && desiredCapabilities == null) {
                startServerAndDevice();
                DesiredCapabilities cap = setAndroidBrowserCapabilities();
                startFirstBrowserDriver(cap, urlOrRelativeUrl);
                if (!emulatorName.isEmpty()) {
                    setDevice(getDriver().getCapabilities().asMap().get("deviceUDID").toString(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
                    attachShutDownHookStopEmulator(getServices(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
                }
                putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
            } else {
                driver = 1;
                DesiredCapabilities cap = setAndroidBrowserCapabilities();
                if (appiumUrl.isEmpty()) {
                    putAllureParameter("Using Appium port", usePort.get(0));
                } else {
                    putAllureParameter("Using Appium url", appiumUrl);
                }
                startFirstBrowserDriver(cap, urlOrRelativeUrl);
                putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
            }
        } else {
            startSelenideDriver(urlOrRelativeUrl);
        }
        emulatorName = "";
        putAllureParameter("Browser", browser);
    }

    protected static void navigateURL(String urlOrRelativeUrl) {
        iOSTesting = false;
        urlOrRelativeUrl = baseUrl + urlOrRelativeUrl;
        if (deviceTests) {
            getDriver().get(urlOrRelativeUrl);
        } else {
            WebDriverRunner.getWebDriver().navigate().to(urlOrRelativeUrl);
        }
    }

    public static void openNewBrowser(String urlOrRelativeUrl) {
        iOSTesting = false;
        if (deviceTests) {
            urlOrRelativeUrl = baseUrl + urlOrRelativeUrl;
            startServerAndDevice();
            DesiredCapabilities cap = setAndroidBrowserCapabilities();
            startBrowserDriver(cap, urlOrRelativeUrl);
            if (!emulatorName.isEmpty()) {
                setDevice(getDriver().getCapabilities().asMap().get("deviceUDID").toString(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
                attachShutDownHookStopEmulator(getServices(), getDriver().getCapabilities().asMap().get("deviceUDID").toString());
            }
            putAllureParameter("Version", getDriver().getCapabilities().asMap().get("platformVersion").toString());
        } else {
            startSelenideDriver(urlOrRelativeUrl);
        }
        putAllureParameter("Browser", browser);
    }
}
