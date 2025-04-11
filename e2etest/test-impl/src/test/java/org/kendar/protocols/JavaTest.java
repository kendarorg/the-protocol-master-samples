package org.kendar.protocols;

import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class JavaTest extends BasicTest {
    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        tearDownAfterClassBase();
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        setupDirectories("java");
        var exposed = setupContainer("java-tpm");
        //TPM
        withExposedService("java-tpm", 3306);
        withExposedService("java-tpm", 1883);

        //MySQL server
        withExposedService("java-mysql", 3306);

        //Http server
        withExposedService("java-quote-generator", 80);

        //With backend server
        withExposedService("java-rest", 80);
        withExposedService("java-mosquitto", 1883);

        startContainers();

    }

    @AfterEach
    public void tearDownAfterEach() throws Exception {
        writeScenario();
        tearDownAfterEachBase();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        beforeEachBase(testInfo);
        cleanUpDb();
    }

    @Test
    void A_testDockerIsUp() throws Exception {
        navigateTo("http://java-tpm:8081/api/status", false);
        assertTrue(
                check(() -> getPageSource().contains("\"OK\"")),
                "Unreachable http://java-tpm:8081/api/status");

        navigateTo("http://java-rest/api/status", false);
        assertTrue(
                check(() -> !getPageSource().contains("\"OK\"")),
                "Unreachable http://java-rest/api/status");

        navigateTo("http://java-quote-generator/api/status", false);
        assertTrue(
                check(() -> !getPageSource().contains("\"OK\"")),
                "Unreachable http://java-rest/api/status");
    }

    @Test
    void B_testNavigation() {
        navigateTo("about:blank");
        Sleeper.sleep(1000);
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(15000, () -> getDriver().getPageSource().contains("META"));
        alertWhenHumanDriven("Quotation found");
        Sleeper.sleep(1000);
        newTab("chart");
        navigateTo("about:blank");
        Sleeper.sleep(1000);
        navigateTo("http://java-rest/single.html?symbol=META");

        for (var i = 0; i < 60; i++) {
            Sleeper.sleep(1000);
            var ci = countItems();
            if (ci > 5) break;
            alertWhenHumanDriven("Waited " + i + " seconds - items: " + ci);
        }

        alertWhenHumanDriven("Verify the DB content");
        //Direct sql call to verify the content of the DB
        var ci = countItems();
        System.out.println("Counted items: " + ci);
        assertTrue(ci >= 5);

        alertWhenHumanDriven("Navigation concluded with " + ci);
    }

    private void cleanUpDb() {
        alertWhenHumanDriven("Cleaning up database");
        try {
            var mySqlHost = getEnvironment().getServiceHost("java-mysql", 3306);
            var mySqlPort = getEnvironment().getServicePort("java-mysql", 3306);
            Class.forName("com.mysql.cj.jdbc.Driver");
            var c = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s:%d/db", mySqlHost, mySqlPort),
                            "root", "password");
            var stmt = c.createStatement();
            stmt.execute("DELETE FROM quotation");
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int countItems(String dateTime) {
        try {
            var query = "SELECT COUNT(*) FROM quotation";
            if (dateTime != null) {
                query = query + " WHERE `date` >= '" + dateTime + "'";
            }
            var mySqlHost = getEnvironment().getServiceHost("java-mysql", 3306);
            var mySqlPort = getEnvironment().getServicePort("java-mysql", 3306);
            Class.forName("com.mysql.cj.jdbc.Driver");
            var c = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s:%d/db", mySqlHost, mySqlPort),
                            "root", "password");
            var stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            //Retrieving the result
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            stmt.close();
            c.close();
            return count;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int countItems() {
        return countItems(null);

    }

    @Test
    void C_testRecording() throws Exception {

        try {
            recordingData();

            replayWithoutContainer("java-quote-generator");
            sendFakeMessages();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void replayWithoutContainer(String container) throws Exception {
        stopContainer(container);
        alertWhenHumanDriven("Stopped " + container + " container");
        Sleeper.sleep(1000);
        switchToTab("main");
        navigateTo("about:blank");
        switchToTab("chart");
        navigateTo("about:blank");
        switchToTab("tpm");
        scrollFind("mqtt01panel");
        executeScript("closeAccordion('collapseWildcard')");
        executeScript("openAccordion('collapsemqtt01')");
        executeScript("getData('/api/protocols/mqtt-01/plugins/replay-plugin/start','GET',()=>reloadProtocolmqtt01()." +
                "then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Start replaying with fake mqtt");
        switchToTab("main");

        Sleeper.sleep(1000);
        alertWhenHumanDriven("Cleaning cache and cookies");
        cleanBrowserCache();
        Sleeper.sleep(1000);
        B_testNavigation();
        alertWhenHumanDriven("No Mqtt Replaying completed");
        switchToTab("tpm");
        executeScript("getData('/api/protocols/mqtt-01/plugins/replay-plugin/stop','GET',()=>reloadProtocolmqtt01()." +
                "then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Stopped mqtt replaying");
    }


    private void recordingData() {
        cleanBrowserCache();
        cleanUpDb();
        newTab("tpm");
        alertWhenHumanDriven("Starting the recording");
        Sleeper.sleep(1000);
        //Open tpm
        navigateTo("http://java-tpm:8081/plugins");
        Sleeper.sleep(1000);

        executeScript("openAccordion('collapseWildcard')");
        cleanUpDb();
        executeScript("getData('/api/protocols/all/plugins/record-plugin/start','GET',reloadAllPlugins)");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Executing operations to record");
        switchToTab("main");

        B_testNavigation();
        switchToTab("tpm");
        executeScript("getData('/api/protocols/all/plugins/record-plugin/stop','GET',reloadAllPlugins)");
        Sleeper.sleep(1000);
        cleanUpDb();

        alertWhenHumanDriven("Recording completed");
        var fileContent = httpGetBinaryFile("http://java-tpm:8081/api/global/storage");
        try {
            Files.write(Path.of("target", "JavaTests.zip"), fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ZoneId getServerTimeZone() {
        var data = httpGet("http://java-rest/api/quotation/timezone");
        return ZoneId.of(data);
    }


    private void sendFakeMessages() {
        cleanBrowserCache();
        cleanUpDb();
        Sleeper.sleep(1000);
        var ld = LocalDateTime.now();
        var expectedTime = ld.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        var millis = ZonedDateTime.of(ld, getServerTimeZone()).toInstant().toEpochMilli();
        switchToTab("main");
        navigateTo("about:blank");//itemUpdateMETA
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        switchToTab("tpm");
        //Open tpm
        navigateTo("http://java-tpm:8081/plugins/mqtt-01/publish-plugin");
        executeScript("getData('/api/protocols/mqtt-01/plugins/publish-plugin/start','GET',()=>location.reload())");
        Sleeper.sleep(1000);
        executeScript("openAccordion('collapseSpecificPlugin')");
        selectItem("contentType", "application/json");
        var body = "{ \"symbol\" : \"META\", \"date\" : " +
                millis +
                ",\"price\" : 2000,  \"volume\" : 2000 }";
        fillItem("body", body);
        fillItem("topic", "quotations");
        cleanUpDb();
        executeScript("sendQueueData()");
        Sleeper.sleep(1000);
        //Check on the quotations
        alertWhenHumanDriven("Waiting for META values to update");

        switchToTab("main");
        AtomicInteger reload = new AtomicInteger(0);
        Sleeper.sleep(6000, () -> {
            navigateTo("http://java-rest/api/quotation/quotes/META?ld=" + reload.getAndIncrement());
            Sleeper.sleep(100);
            var source = getDriver().getPageSource();
            return source.contains("META") && source.contains(expectedTime.replace(' ', 'T'));
        });

    }
}
