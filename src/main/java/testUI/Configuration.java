package testUI;

import cucumber.api.Scenario;
import io.netty.handler.logging.LogLevel;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.List;

public class Configuration extends SelenideConfiguration {
    public static final String ANDROID_PLATFORM = "android";
    public static final String IOS_PLATFORM = "ios";
    public static final String DESKTOP_PLATFORM = "desktop";

    // IOS CAPABILITIES
    public static int wdaPort = 8100;
    public static String iOSAppPath = "";
    public static String iOSDeviceName = "";
    public static String iOSVersion = "";
    public static boolean useNewWDA = true;
    public static String xcodeOrgId = "";
    public static String xcodeSigningId = "";
    public static String bundleId = "";
    public static String updatedWDABundleId = "";

    // ANDROID CAPABILITIES
    public static String appPackage = "";
    public static String appActivity = "";
    public static boolean useEmulators = true;
    public static boolean useW3C = false;
    public static String androidDeviceName = "";
    public static String androidAppPath = "";
    public static String androidVersion = "";
    public static String chromeDriverPath = "";
    public static boolean installMobileChromeDriver = true;
    public static int chromeDriverPort = 0;
    public static int systemPort = 0;
    public static String emulatorName = "";

    // COMMON
    public static String automationType = DESKTOP_PLATFORM;
    public static String baseUrl = "";
    public static int timeout = 5;
    public static int baseAppiumPort = 9586;
    public static int baseAppiumBootstrapPort = 5333;
    public static boolean useAllure = false;
    public static int driver = 1;
    public static String UDID = "";
    public static String serverLogLevel = "error";
    public static String screenshotPath = "";
    public static int timeStartAppiumServer = 20;
    public static boolean cleanStart = true;
    public static LogLevel testUILogLevel = LogLevel.INFO;
    public static int launchAppTimeout = 20000;
    public static String appiumUrl = "";
    public static DesiredCapabilities addMobileDesiredCapabilities = new DesiredCapabilities();
    public static String automationName = "";
    public static int poolingInterval = 200;
    public static boolean softAsserts = false;
    public static ArrayList<String> testUIErrors = new ArrayList<>();


    public static String browser = "chrome";
    protected static DesiredCapabilities desiredCapabilities;
    protected static int iOSDevices = 0;
    protected static ThreadLocal<String> firstEmulatorName = new ThreadLocal<>();

    private static ThreadLocal<List<String>> usePort = new ThreadLocal<>();
    private static ThreadLocal<List<String>> useBootstrapPort = new ThreadLocal<>();

    public static List<String> getUsePort() {
        if (usePort.get() == null)
            return new ArrayList<>();
        return usePort.get();
    }

    public static void setUsePort(String port) {
        List<String> ports = new ArrayList<>(getUsePort());
        ports.add(port);
        usePort.set(ports);
    }

    public static void removeUsePort(int driver) {
        List<String> ports = new ArrayList<>(getUsePort());
        if (ports.size() > driver)
            ports.remove(driver);
        usePort.set(ports);
    }

    public static List<String> getUseBootstrapPort() {
        if (useBootstrapPort.get() == null)
            return new ArrayList<>();
        return useBootstrapPort.get();
    }

    public static void removeUseBootstrapPort(int driver) {
        List<String> ports = new ArrayList<>(getUseBootstrapPort());
        if (ports.size() > driver)
            ports.remove(driver);
        useBootstrapPort.set(ports);
    }

    public static void setUseBootstrapPort(String port) {
        List<String> ports = new ArrayList<>(getUseBootstrapPort());
        ports.add(port);
        useBootstrapPort.set(ports);
    }

    public static void putDataCucumber(Scenario scenario) {
        if (!Configuration.automationType.equals(DESKTOP_PLATFORM)) {
            if (Configuration.automationType.equals(Configuration.IOS_PLATFORM)) {
                scenario.write("iOS Device: " + Configuration.iOSDeviceName);
                scenario.write("iOS Version: " + Configuration.iOSVersion);
            } else {
                scenario.write("Android Device: " + Configuration.androidDeviceName);
                scenario.write("Android Version: " + Configuration.androidVersion);
            }
        } else {
            scenario.write("Laptop Browser Testing");
            scenario.write("Browser: " + Configuration.browser);
        }
    }
}
