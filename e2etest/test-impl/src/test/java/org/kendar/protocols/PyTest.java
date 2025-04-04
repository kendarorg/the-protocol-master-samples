package org.kendar.protocols;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class PyTest extends BasicTest{
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
        Sleeper.sleep(500);
        navigateTo("http://py-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(15000,()-> getDriver().getPageSource().contains("META"));
        newTab("chart");
        navigateTo("http://py-rest/single.html?symbol=META");
        alertWhenHumanDriven("Write some data on the db");
        Sleeper.sleep(60000);

        alertWhenHumanDriven("Verify the DB content");
        //Direct sql call to verify the content of the DB

        alertWhenHumanDriven("Navigation concluded");
    }

    @Test
    void C_testRecording() throws Exception {

        try{
            recordingData();

            replayWithoutContainer("py-quote-generator");

            sendFakeMessages();

            replayWithoutContainer("py-rabbit");
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void replayWithoutContainer(String container) throws Exception {
        stopContainer(container);
        alertWhenHumanDriven("Stopped "+container+" container");
        Sleeper.sleep(1000);
        scrollFind("amqp01panel");
        executeScript("toggleAccordion('collapseWildcard')");
        executeScript("toggleAccordion('collapseamqp01')");
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

    private void cleanUpDb(){
        alertWhenHumanDriven("Cleaning up database");
        try (var client = getHttpClient()) {
            var httpget = new HttpDelete("http://py-rest/api/quotation/symbols");
            var httpresponse = client.execute(httpget);

            var baos = new ByteArrayOutputStream();
            httpresponse.getEntity().writeTo(baos);
            assertEquals(200,httpresponse.getCode());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        executeScript("toggleAccordion('collapseWildcard')");
        executeScript("getData('/api/protocols/all/plugins/record-plugin/start','GET',reloadAllPlugins)");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Executing operations to record");
        switchToTab("main");

        B_testNavigation();
        switchToTab("tpm");
        executeScript("getData('/api/protocols/all/plugins/record-plugin/stop','GET',reloadAllPlugins)");
        Sleeper.sleep(1000);

        alertWhenHumanDriven("Recording completed");
    }



    private void sendFakeMessages() {
        cleanBrowserCache();
        cleanUpDb();
        switchToTab("tpm");
        //Open tpm
        navigateTo("http://py-tpm:8081/plugins/amqp-01/publish-plugin");
        Sleeper.sleep(1000);
        executeScript("toggleAccordion('collapseSpecificPlugin')");
        selectItem("contentType", "application/json");
        fillItem("body", "{ajson}");
        executeScript("sendQueueData()");
        Sleeper.sleep(1000);
        //Check on the quotations
        switchToTab("main");
        navigateTo("http://py-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(6000,()-> getDriver().getPageSource().contains("META"));

    }
}
