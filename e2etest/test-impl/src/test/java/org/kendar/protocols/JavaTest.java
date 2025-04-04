package org.kendar.protocols;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class JavaTest extends BasicTest{
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
    @Disabled("Only to run manually to verify the correctness")
    void B_testNavigation() {
        navigateTo("about:blank");
        Sleeper.sleep(500);
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(15000,()-> getDriver().getPageSource().contains("META"));
        newTab("chart");
        navigateTo("http://java-rest/single.html?symbol=META");
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

            replayWithoutContainer("java-quote-generator");

            sendFakeMessages();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void replayWithoutContainer(String container) throws Exception {
        stopContainer(container);
        alertWhenHumanDriven("Stopped "+container+" container");
        Sleeper.sleep(1000);
        scrollFind("mqtt01panel");
        executeScript("toggleAccordion('collapseWildcard')");
        executeScript("toggleAccordion('collapsemqtt01')");
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

    private void cleanUpDb(){
        alertWhenHumanDriven("Cleaning up database");
        try (var client = getHttpClient()) {
            var httpget = new HttpDelete("http://java-rest/api/quotation/symbols");
            var httpresponse = client.execute(httpget);

            var baos = new ByteArrayOutputStream();
            httpresponse.getEntity().writeTo(baos);
            assertEquals(200,httpresponse.getCode());
            Sleeper.sleep(1000);
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
        navigateTo("http://java-tpm:8081/plugins");
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
        navigateTo("http://java-tpm:8081/plugins/mqtt-01/publish-plugin");
        Sleeper.sleep(1000);
        executeScript("toggleAccordion('collapseSpecificPlugin')");
        selectItem("contentType", "application/json");
        fillItem("body", "{ajson}");
        executeScript("sendQueueData()");
        Sleeper.sleep(1000);
        //Check on the quotations
        switchToTab("main");
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(6000,()-> getDriver().getPageSource().contains("META"));

    }
}
