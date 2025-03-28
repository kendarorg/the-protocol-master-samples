package org.kendar.protocols;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.io.ByteArrayOutputStream;
import java.sql.DriverManager;

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
    void B_testNavigation() throws Exception {
        navigateTo("about:blank");
        Sleeper.sleep(500);
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(6000,()-> getDriver().getPageSource().contains("META"));
        newTab("chart");
        navigateTo("about:blank");
        navigateTo("http://java-rest/single.html?symbol=META");

        for(var i=0; i<60; i++) {
            Sleeper.sleep(1000);
            alertWhenHumanDriven("Waited "+i+" seconds");
        }


        alertWhenHumanDriven("Verify the DB content");
        var count= countItems();
        assertTrue(count>=6);

        alertWhenHumanDriven("Navigation concluded, records on DB: "+count);
        Sleeper.sleep(1000);
    }

    @Test
    void C_testRecording() throws Exception {

        try{
            cleanUpDb();
            recordingData();

            cleanUpDb();
            replayWithoutContainer("java-quote-generator");
        }catch(Exception ex){
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @Test
    void D_testFakeMessage() throws Exception {
        stopContainer("java-quote-generator");
        startContainer("java-mosquitto");
        alertWhenHumanDriven("Stopped java-quote-generator container");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Cleaning cache and cookies");
        cleanBrowserCache();
        cleanUpDb();

        cleanBrowserCache();
        cleanUpDb();
        alertWhenHumanDriven("Reload list of quotations");
        newTab("main");
        navigateTo("about:blank");
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        Sleeper.sleep(1000);

        newTab("chart");
        alertWhenHumanDriven("Reload charts");

        navigateTo("about:blank");
        navigateTo("http://java-rest/single.html?symbol=META");
        Sleeper.sleep(1000);

        sendFakeMessageUi("7777","8888");
        alertWhenHumanDriven("Waiting for META values to update");
        Sleeper.sleep(6000,()-> getDriver().getPageSource().contains("META"));

        var itemsCount = countItems();
        assertEquals(1,itemsCount);
        switchToTab("chart");
        Sleeper.sleep(1000);
        switchToTab("main");
        navigateTo("about:blank");
        navigateTo("http://java-rest/index.html");
        switchToTab("chart");
        navigateTo("about:blank");
        navigateTo("http://java-rest/single.html?symbol=META");
        Sleeper.sleep(1000);
        sendFakeMessageUi("8888","9999");
        switchToTab("chart");
        navigateTo("http://java-rest/single.html?symbol=META");
        Sleeper.sleep(1000);
        assertTrue(countItems()>=2);
    }

    private void replayWithoutContainer(String container) throws Exception {
        stopContainer(container);
        alertWhenHumanDriven("Stopped "+container+" container");
        Sleeper.sleep(1000);
        scrollFind("mqtt01panel");
        executeScript("closeAccordion('collapseWildcard')");
        executeScript("openAccordion('collapsemqtt01')");
        executeScript("getData('/api/protocols/mqtt-01/plugins/replay-plugin/start','GET',()=>reloadProtocolmqtt01()." +
                "then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Start replaying with fake mqtt");
        switchToTab("main");

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void recordingData() throws Exception {
        cleanBrowserCache();
        cleanUpDb();
        newTab("tpm");
        alertWhenHumanDriven("Starting the recording");
        Sleeper.sleep(1000);
        //Open tpm
        navigateTo("http://java-tpm:8081/plugins");
        Sleeper.sleep(1000);

        executeScript("openAccordion('collapseWildcard')");
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



    private int countItems() throws Exception {

        com.mysql.cj.jdbc.Driver driver=null;
        Class.forName("com.mysql.cj.jdbc.Driver");
        var connection = DriverManager
                .getConnection(String.format("jdbc:mysql://%s:%d/db",
                                getProxyHost(),
                                getEnvironment().getServicePort("java-mysql",3306)),
                        "root", "password");
        var rs = connection.createStatement().executeQuery("select count(*) from quotation");
        rs.next();

        var count = rs.getInt(1);
        rs.close();
        connection.close();
        return count;
    }

    private void sendFakeMessageUi(String price, String volume) {


        newTab("tpm");
        //Open tpm
        navigateTo("http://java-tpm:8081/plugins/mqtt-01/publish-plugin");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Enabling publish plugin");
        executeScript("openAccordion('collapseStatus')");
        executeScript("getData('/api/protocols/mqtt-01/plugins/publish-plugin/start','GET',()=>location.reload())");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Filling data");
        executeScript("closeAccordion('collapseStatus')");
        executeScript("openAccordion('collapseSpecificPlugin')");
        selectItem("contentType", "application/json");
        var body = "{ \"symbol\" : \"META\", \"date\" : " +
                System.currentTimeMillis() +
                ",\"price\" : "+price+",  \"volume\" : "+volume+" }";
        fillItem("body", body);
        fillItem("topic","quotations");
        
        executeScript("sendQueueData()");
        Sleeper.sleep(1000);

    }
}
