package TestRunners;

import io.netty.handler.logging.LogLevel;
import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import pages.GoogleLandingPage;
import testUI.Configuration;

import static testUI.TestUIDriver.*;
import static testUI.TestUIServer.stop;
import static testUI.UIOpen.open;
import static testUI.UIUtils.*;
import static testUI.Utils.AppiumHelps.sleep;
import static testUI.Utils.By.*;
import static testUI.Utils.Performance.getListOfCommandsTime;
import static testUI.Utils.Performance.logAverageTime;
import static testUI.elements.TestUI.E;
import static testUI.elements.TestUI.raiseSoftAsserts;

public class TestBrowser {
    private GoogleLandingPage googleLandingPage = new GoogleLandingPage();

    @Test
    @DisplayName("Laptop browser test case")
    public void testDesktopBrowser() {
        Configuration.automationType = DESKTOP_PLATFORM;
        Configuration.testUILogLevel = LogLevel.DEBUG;
        Configuration.softAsserts = true;
        Configuration.browser = "chrome";
        open("https://www.google.com");
        UIAssert("the url is not correct",
                getSelenideDriver().getCurrentUrl().equals("https://www.google.com/"));
        executeJs("arguments[0].value='TestUI';", googleLandingPage.getGoogleSearchInput()
                .getSelenideElement().getWrappedElement());
        googleLandingPage.getGoogleSearch()
                .then().saveScreenshot("~/Documents" +
                "/screen" +
                ".png");
        logAverageTime();
        System.out.println(getListOfCommandsTime());

        raiseSoftAsserts();
    }

    @Test
    public void setDriverTest() {
        ChromeOptions options = new ChromeOptions();
        Configuration.softAsserts = false;
        options.addArguments("--user-agent=Agent", "--ignore-certificate-errors");
        Configuration.chromeOptions = options;
        selenideBrowserCapabilities.setBrowserName("chrome");
        open("https://www.whatsmyua.info/");
        E(byCssSelector("textarea")).waitFor(10).untilHasText("Agent");
        sleep(1000);
    }

    @Test
    @DisplayName("Laptop browser test case")
    public void testDesktopBrowserSafari() {
        Configuration.automationType = DESKTOP_PLATFORM;
        Configuration.browser = "safari";
        Configuration.serverLogLevel = "all";
        Configuration.softAsserts = true;
        open("https://www.google.com");
        System.out.println(getTestUIDriver().getCurrentUrl());
        executeJs("arguments[0].value='TestUI';", googleLandingPage.getGoogleSearchInput()
                .getSelenideElement().getWrappedElement());
        googleLandingPage.getGoogleSearch().given()
                .then().click().saveScreenshot("/Users/alvarolasernalopez/Documents/screen" +
                ".png");
        stop();
        open("https://www.google.com");
        googleLandingPage.getGoogleSearch().given().waitFor(10).untilIsVisible()
                .then().click().saveScreenshot("/Users/alvarolasernalopez/Documents/screen" +
                ".png");

        raiseSoftAsserts();
    }


    @Test
    @DisplayName("Laptop browser test case, assert status code")
    public void testDesktopBrowserStatusCode() {
        Configuration.automationType = DESKTOP_PLATFORM;
        Configuration.logNetworkCalls = true;
        Configuration.browser = "chrome";
        open("https://www.google.com")
                .getNetworkCalls().logAllCalls().filterByExactUrl("https://www.google.com/")
                .logFilteredCalls()
                .and()
                .filterByUrl("https://www.google.com/").assertFilteredCallExists()
                .logFilteredCalls().assertStatusCode(200)
                .assertResponseHeader("Content-Type", "text/html; charset=UTF-8");

        stop();
        open("https://www.google.com")
                .getLastNetworkCalls(100).logAllCalls()
                .filterByUrl("https://www.google.com/").logFilteredCalls()
                .assertFilteredCallExists();
        stop();
    }

    @Test
    @DisplayName("Laptop browser test case")
    public void testDesktopCustomDriverBrowser() {
        Configuration.automationType = DESKTOP_PLATFORM;
        Configuration.browser = "chrome";
        open("https://www.google.com");
        stop();
        ChromeOptions options = new ChromeOptions();
        String userAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";
        options.addArguments("--user-agent=" + userAgent);
        ChromeDriver chromeDriver = new ChromeDriver(options);
        setDriver(chromeDriver);
        open("https://www.whatsmyua.info/");
        E(byCssSelector("textarea")).waitFor(10).untilHasText(userAgent);
        stop();
        open("https://www.google.com");
        stop();
    }

    @Test
    @DisplayName("Laptop browser test case one line code")
    public void testAndroidBrowserOneLine() {
        Configuration.automationType = DESKTOP_PLATFORM;
        Configuration.useAllure = false;
        Configuration.softAsserts = true;
        Configuration.browser = "chrome";
        Configuration.testUILogLevel = LogLevel.DEBUG;
        open("https://loadero.com/login")
                .given("I set element").setElement(byCssSelector("#username"))
                .and("I check if visible").waitFor(5).untilIsVisible()
                .and("I send keys").setValueJs("\\uD83D\\uDE00")
                .given("I set element").setElement(byCssSelector("#password"))
                .and("I check if visible").waitFor(5).untilIsVisible()
                .and("I send keys").setValueJs("password")
                .then("I find the submit").setElement(byCssSelector("[type=\"submit\"]"))
                .and("I click on it").click();
        raiseSoftAsserts();
    }
}
