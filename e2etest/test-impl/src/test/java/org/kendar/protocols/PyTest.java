package org.kendar.protocols;

import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class PyTest extends BasicTest {
    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        tearDownAfterClassBase();
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        setupDirectories("python");
        var exposed = setupContainer("py-tpm");
        //TPM
        withExposedService("py-tpm", 3306);
        withExposedService("py-tpm", 5672);

        //MySQL server
        withExposedService("py-mysql", 3306);

        //Http server
        withExposedService("py-quote-generator", 80);

        //With backend server
        withExposedService("py-rest", 80);
        withExposedService("py-rabbit", 5672);

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
        navigateTo("http://py-tpm:8081/api/status", false);
        assertTrue(
                check(() -> getPageSource().contains("\"OK\"")),
                "Unreachable http://py-tpm:8081/api/status");

        navigateTo("http://py-rest/api/status", false);
        assertTrue(
                check(() -> !getPageSource().contains("\"OK\"")),
                "Unreachable http://py-rest/api/status");

        navigateTo("http://py-quote-generator/api/status", false);
        assertTrue(
                check(() -> !getPageSource().contains("\"OK\"")),
                "Unreachable http://py-rest/api/status");
    }

    @Test
    void B_testNavigation() {
        navigateTo("about:blank");
        Sleeper.sleep(1000);
        navigateTo("http://py-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(15000, () -> getDriver().getPageSource().contains("META"));
        alertWhenHumanDriven("Quotation found");
        Sleeper.sleep(1000);
        newTab("chart");
        navigateTo("about:blank");
        Sleeper.sleep(1000);
        navigateTo("http://py-rest/single.html?symbol=META");

        for(var i=0; i<60; i++) {
            Sleeper.sleep(1000);
            var ci = countItems();
            alertWhenHumanDriven("Waited "+i+" seconds - items: "+ci);
        }

        alertWhenHumanDriven("Verify the DB content");
        //Direct sql call to verify the content of the DB
        var ci = countItems();
        System.out.println("Counted items: " + ci);
        assertTrue(ci > 0);

        alertWhenHumanDriven("Navigation concluded with "+ci);
    }

    private void cleanUpDb() {
        alertWhenHumanDriven("Cleaning up database");
        try {
            var mySqlHost = getEnvironment().getServiceHost("py-mysql", 3306);
            var mySqlPort = getEnvironment().getServicePort("py-mysql", 3306);
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

    private int countItems() {
        try {
            var mySqlHost = getEnvironment().getServiceHost("py-mysql", 3306);
            var mySqlPort = getEnvironment().getServicePort("py-mysql", 3306);
            Class.forName("com.mysql.cj.jdbc.Driver");
            var c = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s:%d/db", mySqlHost, mySqlPort),
                            "root", "password");
            var stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM quotation");
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

    @Test
    void C_testRecording() throws Exception {

        try {
            recordingData();

            replayWithoutContainer("py-quote-generator");
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
        scrollFind("amqp01panel");
        executeScript("closeAccordion('collapseWildcard')");
        executeScript("openAccordion('collapseamqp01')");
        executeScript("getData('/api/protocols/amqp-01/plugins/replay-plugin/start','GET',()=>reloadProtocolamqp01()." +
                "then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Start replaying with fake amqp");
        switchToTab("main");

        Sleeper.sleep(1000);
        alertWhenHumanDriven("Cleaning cache and cookies");
        cleanBrowserCache();
        Sleeper.sleep(1000);
        B_testNavigation();
        alertWhenHumanDriven("No amqp Replaying completed");
        switchToTab("tpm");
        executeScript("getData('/api/protocols/amqp-01/plugins/replay-plugin/stop','GET',()=>reloadProtocolamqp01()." +
                "then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Stopped amqp replaying");
    }


    private void recordingData() {
        cleanBrowserCache();
        cleanUpDb();
        newTab("tpm");
        alertWhenHumanDriven("Starting the recording");
        Sleeper.sleep(1000);
        //Open tpm
        navigateTo("http://py-tpm:8081/plugins");
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
        var fileContent = httpGetBinaryFile("http://py-tpm:8081/api/global/storage");
        try {
            Files.write(Path.of("target","PyTests.zip"),fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getCurrentLocalDateTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void sendFakeMessages() {
        cleanBrowserCache();
        cleanUpDb();

        switchToTab("main");
        navigateTo("about:blank");//itemUpdateMETA
        navigateTo("http://py-rest/index.html");//itemUpdateMETA
        switchToTab("tpm");
        //Open tpm
        navigateTo("http://py-tpm:8081/plugins/amqp-01/publish-plugin");
        executeScript("getData('/api/protocols/amqp-01/plugins/publish-plugin/start','GET',()=>location.reload())");
        Sleeper.sleep(1000);
        executeScript("openAccordion('collapseSpecificPlugin')");
        selectItem("contentType", "application/json");
        var body = "{ \"symbol\" : \"META\", \"date\" : \"" +
                getCurrentLocalDateTimeStamp() +
                "\",\"price\" : 2000,  \"volume\" : 2000 }";
        fillItem("body",body);
        fillItem("queue","quotations");
        fillItem("exchange","stock");
        executeScript("sendQueueData()");
        Sleeper.sleep(1000);
        //Check on the quotations
        alertWhenHumanDriven("Waiting for META values to update");

        switchToTab("main");
        navigateTo("http://py-rest/index.html");//itemUpdateMETA
        Sleeper.sleep(6000, () -> getDriver().getPageSource().contains("META"));
        assertEquals(1,countItems());

    }
}
