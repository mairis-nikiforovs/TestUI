package testUI;

import io.qameta.allure.Allure;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.proxy.CaptureType;
import org.json.JSONObject;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import testUI.Utils.TestUIException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static testUI.UIUtils.putLog;
import static testUI.Utils.Logger.putLogError;

public class BrowserLogs {

    private List<List<JSONObject>> calls;
    private List<JSONObject> filteredCalls;
    private List<Map<String, String>> callHar;
    private boolean severalFilters;
    private static Proxy seleniumProxy;

    private BrowserLogs(List<List<JSONObject>> calls,
        List<JSONObject> filteredCalls,
        List<Map<String, String>> callHar,
        boolean severalFilters) {
        this.calls = calls;
        this.filteredCalls = filteredCalls;
        this.callHar = callHar;
        this.severalFilters = severalFilters;
    }

    public BrowserLogs() {
    }

    private static BrowserMobProxy proxy;

    public static BrowserMobProxy getProxy() {
        return proxy;
    }

    public static void stopProxy() {
        proxy.stop();
        proxy = null;
    }

    public void setLogs() {
        LoggingPreferences logPrefs = new LoggingPreferences();
        if (Configuration.logNetworkCalls) {
            if (Configuration.remote == null || Configuration.remote.isEmpty()) {
                if (proxy == null || !proxy.isStarted()) {
                    proxy = new BrowserMobProxyServer();
                    // start the proxy
                    proxy.start(0);
                    // get the Selenium proxy object
                    seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
                    seleniumProxy.setHttpProxy("localhost:" + getProxy().getPort());
                    seleniumProxy.setSslProxy("localhost:" + getProxy().getPort());
                    Configuration.selenideBrowserCapabilities.setCapability(
                        CapabilityType.PROXY, seleniumProxy);
                    // enable more detailed HAR capture, if desired (see CaptureType for the complete list)
                    proxy.enableHarCaptureTypes(
                        CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT,
                        CaptureType.REQUEST_HEADERS, CaptureType.RESPONSE_HEADERS);
                    // create a new HAR with the label "Proxy"
                    proxy.newHar("Proxy");
                }
            }
            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        }
        if (Configuration.browserLogs) {
            logPrefs.enable(LogType.BROWSER, Level.ALL);
        }
        if (Configuration.browserLogs || Configuration.logNetworkCalls) {
            Configuration.selenideBrowserCapabilities.setCapability("goog:loggingPrefs", logPrefs);
            Configuration.selenideBrowserCapabilities.setCapability(
                CapabilityType.LOGGING_PREFS, logPrefs);
        }
    }

    public Proxy getSeleniumProxy() {
        return seleniumProxy;
    }

    public BrowserLogs getNetworkCalls() {
        List<List<JSONObject>> calls = new ArrayList<>();
        List<Map<String, String>> callHar = new ArrayList<>();
        if (Configuration.remote != null && !Configuration.remote.isEmpty()) {
            try {
                for (LogEntry list : getWebDriver().manage().logs().get(LogType.PERFORMANCE)
                                                   .getAll()) {
                    JSONObject obj = new JSONObject(list.getMessage());
                    JSONObject requests = obj.getJSONObject("message").getJSONObject("params");
                    Map<String, String> call = new HashMap<>();
                    if (requests.has("request") &&
                        requests.getJSONObject("request").has("url")) {
                        call.put("URL", requests.getJSONObject("request").getString("url"));
                        if (requests.getJSONObject("request").has("status")) {
                            call.put(
                                "Status",
                                String.valueOf(requests.getJSONObject("request")
                                                       .getInt("status"))
                            );
                        } else {
                            call.put("Status", "0");
                        }
                        if (requests.getJSONObject("request").has("headers")) {
                            call.put(
                                "RequestHeaders",
                                requests.getJSONObject("request").getJSONObject("headers")
                                        .toString()
                            );
                        } else {
                            call.put("RequestHeaders", "");
                        }
                    } else if (requests.has("response") &&
                               requests.getJSONObject("response").has("url")) {
                        call.put("URL", requests.getJSONObject("response").getString("url"));
                        if (requests.getJSONObject("response").has("status")) {
                            call.put(
                                "Status",
                                String.valueOf(requests.getJSONObject("response")
                                                       .getInt("status"))
                            );
                        } else {
                            call.put("Status", "0");
                        }
                        if (requests.getJSONObject("response").has("headers")) {
                            call.put(
                                "ResponseHeaders",
                                requests.getJSONObject("response").getJSONObject("headers")
                                        .toString()
                            );
                        } else {
                            call.put("RequestHeaders", "");
                        }
                    }
                    if (!call.isEmpty()) {
                        callHar.add(call);
                        call = new HashMap<>();
                    }
                    if (requests.has("redirectResponse") &&
                        requests.getJSONObject("redirectResponse").has("url")) {
                        call.put("URL", requests.getJSONObject("redirectResponse")
                                                .getString("url"));
                        if (requests.getJSONObject("redirectResponse").has("status")) {
                            call.put(
                                "Status",
                                String.valueOf(requests.getJSONObject("redirectResponse")
                                                       .getInt("status"))
                            );
                        } else {
                            call.put("Status", "0");
                        }
                        if (requests.getJSONObject("redirectResponse").has("headers")) {
                            call.put(
                                "ResponseHeaders",
                                requests.getJSONObject("redirectResponse")
                                        .getJSONObject("headers").toString()
                            );
                        } else {
                            call.put("ResponseHeaders", "");
                        }
                    }
                    if (!call.isEmpty()) {
                        callHar.add(call);
                    }
                }
            } catch (UnsupportedCommandException | JsonException e) {
                putLog("The PERFORMANCE logs are not supported for this browser");
            }
        } else {
            if (getProxy() != null) {
                Har har = getProxy().getHar();
                for (HarEntry entry : har.getLog().getEntries()) {
                    Map<String, String> call = new HashMap<>();
                    call.put("URL", entry.getRequest().getUrl());
                    call.put("Status", String.valueOf(entry.getResponse().getStatus()));
                    call.put("RequestHeaders", entry.getRequest().getHeaders().toString());
                    call.put("ResponseHeaders", entry.getResponse().getHeaders().toString());
                    String payload = entry.getResponse().getContent().getText() == null ?
                        "" : entry.getResponse().getContent().getText();
                    call.put("Payload", payload);
                    if (entry.getResponse().getStatus() >= 300) {
                        call.put("Response: ", String.valueOf(entry.getResponse().getContent()
                                                                   .getText()));
                    }
                    callHar.add(call);
                }
            }
        }
        return new BrowserLogs(calls, new ArrayList<>(), callHar, false);
    }

    public List<String> getBrowserLogs() {
        List<String> browserLogs = new ArrayList<>();
        try {
            for (LogEntry log : getWebDriver().manage().logs().get(LogType.BROWSER)) {
                Date date = new Date(log.getTimestamp());
                SimpleDateFormat jdf = new SimpleDateFormat("YYYY.MM.dd HH:mm:ss.SSS");

                browserLogs.add(
                    "[" + log.getLevel() + "] " + jdf.format(date) + ": " + log.getMessage()
                );
            }
        } catch (UnsupportedCommandException e) {
            putLogError("The BROWSER logs are not supported for this browser");
        } catch (JsonException e) {
            putLogError("Error parsing the BROWSER logs: \n");
            e.printStackTrace();
        }
        return browserLogs;
    }

    public BrowserLogs getLastNetworkCalls(int lastX) {
        List<List<JSONObject>> calls = new ArrayList<>();
        List<Map<String, String>> callHar = new ArrayList<>();
        if (Configuration.remote != null && !Configuration.remote.isEmpty()) {
            List<LogEntry> list = getWebDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
            if (list.size() < lastX) {
                lastX = list.size();
            }
            for (int j = list.size() - lastX; j < list.size(); j++) {
                LogEntry logEntry = list.get(j);
                JSONObject obj = new JSONObject(logEntry.getMessage());
                JSONObject requests = obj.getJSONObject("message").getJSONObject("params");
                if (requests.has("requestId")) {
                    if (requests.has("requestId")) {
                        Map<String, String> call = new HashMap<>();
                        if (requests.has("request") &&
                            requests.getJSONObject("request").has("url")) {
                            call.put("URL", requests.getJSONObject("request").getString("url"));
                            if (requests.getJSONObject("request").has("status")) {
                                call.put("Status", requests.getJSONObject("request")
                                                           .getString("status"));
                            }
                            if (requests.getJSONObject("request").has("headers")) {
                                call.put(
                                    "RequestHeaders",
                                    requests.getJSONObject("request").getJSONObject("headers")
                                            .toString()
                                );
                            }
                        } else if (requests.has("response") &&
                                   requests.getJSONObject("response").has("url")) {
                            call.put(
                                "URL",
                                requests.getJSONObject("response").getString("url")
                            );
                            if (requests.getJSONObject("response").has("status")) {
                                call.put(
                                    "Status",
                                    String.valueOf(requests.getJSONObject("response")
                                                           .getInt("status"))
                                );
                            }
                            if (requests.getJSONObject("response").has("headers")) {
                                call.put(
                                    "ResponseHeaders",
                                    requests.getJSONObject("response")
                                            .getJSONObject("headers").toString()
                                );
                            }
                        }
                        if (!call.isEmpty()) {
                            callHar.add(call);
                        }
                    }
                }
            }
        } else {
            Har har = getProxy().getHar();
            List<HarEntry> entryList = har.getLog().getEntries();
            if (entryList.size() < lastX) {
                lastX = entryList.size();
            }
            for (int j = entryList.size() - lastX; j < entryList.size(); j++) {
                Map<String, String> call = new HashMap<>();
                call.put("URL", entryList.get(j).getRequest().getUrl());
                call.put("Status", String.valueOf(entryList.get(j).getResponse().getStatus()));
                call.put("RequestHeaders", entryList.get(j).getRequest().getHeaders().toString());
                call.put("ResponseHeaders", entryList.get(j).getResponse().getHeaders().toString());
                String payload = entryList.get(j).getResponse().getContent().getText() == null ?
                    "" : entryList.get(j).getResponse().getContent().getText();
                call.put("Payload", payload);
                if (entryList.get(j).getResponse().getStatus() >= 300) {
                    call.put("Response: ", String.valueOf(entryList.get(j).getResponse()
                                                                   .getContent().getText()));
                }
                callHar.add(call);
            }
        }
        return new BrowserLogs(calls, new ArrayList<>(), callHar, false);
    }

    public BrowserLogs filterByUrl(String url) {
        List<JSONObject> calls = new ArrayList<>();
        if (!this.severalFilters) {
            for (Map<String, String> call : this.callHar) {
                JSONObject jsonObject = new JSONObject();
                if (call.get("URL").contains(url)) {
                    jsonObject.put("URL", call.get("URL"));
                    jsonObject.put("statusCode", call.get("Status"));
                    if (call.get("Payload") != null) {
                        jsonObject.put("Payload", call.get("Payload"));
                    }
                    if (call.get("RequestHeaders") != null) {
                        jsonObject.put("RequestHeaders", call.get("RequestHeaders"));
                    }
                    if (call.get("ResponseHeaders") != null) {
                        jsonObject.put("ResponseHeaders", call.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject);
                }
            }
        } else {
            for (JSONObject jsonObject : this.filteredCalls) {
                JSONObject jsonObject2 = new JSONObject();
                if (jsonObject.get("URL").toString().contains(url)) {
                    jsonObject2.put("URL", jsonObject.get("URL"));
                    jsonObject2.put("statusCode", jsonObject.get("statusCode"));
                    if (jsonObject.has("Payload")) {
                        jsonObject2.put("Payload", jsonObject.get("Payload"));
                    }
                    if (jsonObject.has("RequestHeaders")) {
                        jsonObject2.put("RequestHeaders", jsonObject.get("RequestHeaders"));
                    }
                    if (jsonObject.has("ResponseHeaders")) {
                        jsonObject2.put("ResponseHeaders", jsonObject.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject2);
                }
            }
        }
        return new BrowserLogs(this.calls, calls, this.callHar, this.severalFilters);
    }

    public BrowserLogs filterByExactUrl(String url) {
        List<JSONObject> calls = new ArrayList<>();
        if (!this.severalFilters) {
            for (Map<String, String> call : this.callHar) {
                JSONObject jsonObject = new JSONObject();
                if (call.get("URL").equals(url)) {
                    jsonObject.put("URL", call.get("URL"));
                    jsonObject.put("statusCode", call.get("Status"));
                    if (call.get("Payload") != null) {
                        jsonObject.put("Payload", call.get("Payload"));
                    }
                    if (call.get("RequestHeaders") != null) {
                        jsonObject.put("RequestHeaders", call.get("RequestHeaders"));
                    }
                    if (call.get("ResponseHeaders") != null) {
                        jsonObject.put("ResponseHeaders", call.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject);
                }
            }
        } else {
            for (JSONObject jsonObject : this.filteredCalls) {
                JSONObject jsonObject2 = new JSONObject();
                if (jsonObject.get("URL").toString().equals(url)) {
                    jsonObject2.put("URL", jsonObject.get("URL"));
                    jsonObject2.put("statusCode", jsonObject.get("statusCode"));
                    if (jsonObject.has("Payload")) {
                        jsonObject2.put("Payload", jsonObject.get("Payload"));
                    }
                    if (jsonObject.has("RequestHeaders")) {
                        jsonObject2.put("RequestHeaders", jsonObject.get("RequestHeaders"));
                    }
                    if (jsonObject.has("ResponseHeaders")) {
                        jsonObject2.put("ResponseHeaders", jsonObject.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject2);
                }
            }
        }
        return new BrowserLogs(this.calls, calls, this.callHar, this.severalFilters);
    }

    public BrowserLogs filterByHeader(String header, String value) {
        List<JSONObject> calls = new ArrayList<>();
        if (!this.severalFilters) {
            for (Map<String, String> call : this.callHar) {
                JSONObject jsonObject = new JSONObject();
                if (call.get("RequestHeaders") != null
                    && call.get("RequestHeaders").toLowerCase().contains(header.toLowerCase())
                    && call.get("RequestHeaders").contains(value)
                    || call.get("ResponseHeaders") != null
                       && call.get("ResponseHeaders").toLowerCase().contains(header.toLowerCase())
                       && call.get("ResponseHeaders").contains(value)) {
                    jsonObject.put("URL", call.get("URL"));
                    jsonObject.put("statusCode", call.get("Status"));
                    if (call.get("Payload") != null) {
                        jsonObject.put("Payload", call.get("Payload"));
                    }
                    if (call.get("RequestHeaders") != null) {
                        jsonObject.put("RequestHeaders", call.get("RequestHeaders"));
                    }
                    if (call.get("ResponseHeaders") != null) {
                        jsonObject.put("ResponseHeaders", call.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject);
                }
            }
        } else {
            for (JSONObject jsonObject : this.filteredCalls) {
                JSONObject jsonObject2 = new JSONObject();
                if (jsonObject.has("RequestHeaders")
                    && jsonObject.getString("RequestHeaders").toLowerCase()
                                 .contains(header.toLowerCase())
                    && jsonObject.getString("RequestHeaders").contains(value)
                    || jsonObject.has("ResponseHeaders")
                       && jsonObject.getString("ResponseHeaders").toLowerCase()
                                    .contains(header.toLowerCase())
                       && jsonObject.getString("ResponseHeaders").contains(value)) {
                    jsonObject2.put("URL", jsonObject.get("URL"));
                    jsonObject2.put("statusCode", jsonObject.get("statusCode"));
                    if (jsonObject.has("Payload")) {
                        jsonObject2.put("Payload", jsonObject.get("Payload"));
                    }
                    if (jsonObject.has("RequestHeaders")) {
                        jsonObject2.put("RequestHeaders", jsonObject.get("RequestHeaders"));
                    }
                    if (jsonObject.has("ResponseHeaders")) {
                        jsonObject2.put("ResponseHeaders", jsonObject.get("ResponseHeaders"));
                    }
                    calls.add(jsonObject2);
                }
            }
        }
        return new BrowserLogs(this.calls, calls, this.callHar, this.severalFilters);
    }


    public BrowserLogs and() {
        return new BrowserLogs(
            this.calls,
            this.filteredCalls,
            this.callHar,
            true
        );
    }

    public BrowserLogs or() {
        return new BrowserLogs(
            this.calls,
            this.filteredCalls,
            this.callHar,
            false
        );
    }

    public BrowserLogs assertStatusCode(int statusCode) {
        for (JSONObject responses : this.filteredCalls) {
            if (responses.getInt("statusCode") != statusCode
                && responses.getInt("statusCode") != 0) {
                if (Configuration.useAllure) {
                    Allure.addAttachment(
                        "Assert Status Code",
                        "Status code should be " +
                        statusCode +
                        " but was " +
                        responses.getInt("statusCode") +
                        "\n Response: \n" +
                        responses
                    );
                }
                throw new TestUIException("Status code should be " + statusCode + " but was "
                                          + responses.getInt("statusCode") + "\n Response: \n" +
                                          responses);
            }
        }
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }

    public BrowserLogs assertStatusCode(int statusCode, int statusCode2) {
        for (JSONObject responses : this.filteredCalls) {
            if ((responses.getInt("statusCode") < statusCode ||
                 responses.getInt("statusCode") > statusCode2)
                && responses.getInt("statusCode") != 0) {
                if (Configuration.useAllure) {
                    Allure.addAttachment(
                        "Assert Status Code",
                        "Status code should be between " +
                        statusCode +
                        " and " +
                        statusCode2 +
                        " but was " +
                        responses.getInt("statusCode") +
                        "\n Response: \n" +
                        responses
                    );
                }
                throw new TestUIException(
                    "Status code should be between " +
                    statusCode +
                    " and " +
                    statusCode2 +
                    " but was " +
                    responses.getInt("statusCode") +
                    "\n Response: \n" +
                    responses
                );
            }
        }
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }


    public BrowserLogs assertResponseHeader(String Header, String Value) {
        boolean found = false;
        for (JSONObject responses : this.filteredCalls) {
            if (responses.has("ResponseHeaders") &&
                !responses.getString("ResponseHeaders").toLowerCase()
                          .contains(Value.toLowerCase())) {
                if (Configuration.useAllure) {
                    Allure.addAttachment(
                        "Assert Headers",
                        "The headers should contain '" +
                        Header +
                        "' Equal to '" +
                        Value +
                        " but is: \n " +
                        responses.getString("ResponseHeaders") +
                        "\n Response: \n" +
                        responses
                    );
                }
                throw new TestUIException(
                    "The headers should contain '" +
                    Header +
                    "' Equal to '" +
                    Value +
                    " but is: \n " +
                    responses.getString("ResponseHeaders") +
                    "\n Response: \n" +
                    responses
                );
            } else if (responses.has("ResponseHeaders") &&
                       responses.getString("ResponseHeaders").contains(Value)) {
                found = true;
            }
        }
        if (!found) {
            throw new TestUIException(
                "There were no calls with those response headers: '" +
                Header +
                "' equal to '" +
                Value + "'"
            );
        }
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }

    public BrowserLogs assertFilteredCallExists() {
        if (this.filteredCalls.size() == 0) {
            throw new TestUIException("There are no network calls with the filter parameters included!");
        }
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }

    public List<JSONObject> extractFiltered() {
        return this.filteredCalls;
    }

    public BrowserLogs logFilteredCalls() {
        if (Configuration.useAllure) {
            Allure.addAttachment("Filtered Calls", this.filteredCalls.toString());
        }
        putLog(this.filteredCalls.toString());
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }

    public BrowserLogs logAllCalls() {
        if (Configuration.useAllure) {
            Allure.addAttachment("Calls", this.callHar.toString());
        }
        putLog(this.callHar.toString());
        return new BrowserLogs(this.calls, this.filteredCalls, this.callHar, this.severalFilters);
    }
}
