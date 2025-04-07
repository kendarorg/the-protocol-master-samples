package org.kendar.protocols;

import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class NetCoreTest extends BasicTest {
    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        tearDownAfterClassBase();
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        setupDirectories("net-core");
        var exposed = setupContainer("net-core-tpm");
        //TPM
        withExposedService("net-core-tpm", 3306);

        //MySQL server
        withExposedService("net-core-mysql", 3306);

        //Http server
        withExposedService("net-core-http", 80);

        //With backend server
        withExposedService("net-core-rest", 80);

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

    private void cleanUpDb() {
        alertWhenHumanDriven("Cleaning up database");
        try {
            var mySqlHost = getEnvironment().getServiceHost("net-core-mysql", 3306);
            var mySqlPort = getEnvironment().getServicePort("net-core-mysql", 3306);
            Class.forName("com.mysql.cj.jdbc.Driver");
            var c = DriverManager
                    .getConnection(String.format("jdbc:mysql://%s:%d/db", mySqlHost, mySqlPort),
                            "root", "password");
            var stmt = c.createStatement();
            stmt.execute("DELETE FROM task");
            stmt.close();
            c.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void A_testDockerIsUp() throws Exception {
        navigateTo("http://net-core-tpm:8081/api/status", false);
        assertTrue(
                check(() -> getPageSource().contains("\"OK\"")),
                "Unreachable http://net-core-tpm:8081/api/status");
        navigateTo("http://net-core-http/status.html", false);
        assertTrue(
                check(() -> getPageSource().contains("\"OK\"")),
                "Unreachable http://net-core-http/api/status");
        navigateTo("http://net-core-rest/api/status", false);
        assertTrue(
                check(() -> !getPageSource().contains("\"OK\"")),
                "Unreachable http://net-core-rest/api/status");
    }

    //@Test
    void B_testNavigation() throws InterruptedException {
        navigateTo("about:blank");
        Sleeper.sleep(500);
        navigateTo("http://net-core-http/index.html");
        Sleeper.sleep(6000);

        alertWhenHumanDriven("Inserting new task");
        //Insert item
        fillItem("taskName", "Laundry");
        selectItem("addPriority", "High");
        fillItem("notes", "Wash Cotton");

        //Submit
        clickItem("submitNewTask");
        Sleeper.sleep(1000);
        takeSnapshot();
        alertWhenHumanDriven("Inserting new task");

        //Find the selected item

        assertNotNull(findElementByXPath("//tbody[@id='listTableBody']/tr/td[contains(text(), \"Laundry\")]"));
        assertNotNull(findElementByXPath("//tbody[@id='listTableBody']/tr/td[contains(text(), \"Wash Cotton\")]"));
        takeSnapshot();


        alertWhenHumanDriven("Completing task");
        //Modify the status
        var status = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/select");
        selectItem(status.get(1), "Completed");
        Sleeper.sleep(1000);
        takeSnapshot();


        //Update
        var button = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/button").get(1);
        button.click();
        Sleeper.sleep(1000);
        takeSnapshot();


        alertWhenHumanDriven("Archiving the task");
        //Reload the stale element

        button = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/button").get(1);
        button.click();
        Sleeper.sleep(1000);
        takeSnapshot();

        //Set the archive

        clickItem("setArchivedTasks");
        Sleeper.sleep(2000);
        takeSnapshot();


        alertWhenHumanDriven("Clean up the task");
        //Delete old item

        button = findElementByXPath("//tbody[@id='archivedTableBody']/tr/td/button");
        button.click();
        Sleeper.sleep(1000);
        var yesButton = findElementByXPath(".//button[contains(text(), \"Yes, delete it!\")]");
        yesButton.click();
        Sleeper.sleep(1000);

        var okButton = findElementByXPath(".//button[contains(text(), \"OK\")]");
        okButton.click();
        Sleeper.sleep(1000);

        alertWhenHumanDriven("Operation completed");
        navigateTo("about:blank");
    }

    @Test
    void C_testRecording() throws Exception {

        try {
            recordingData();

            replayWithoutMysql();

            replayWIthoutBackend();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private void replayWIthoutBackend() throws Exception {
        //Replaying without backend
        stopContainer("net-core-rest");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Stopped backend container");
        scrollFind("http01panel");
        executeScript("closeAccordion('collapsemysql01')");
        executeScript("openAccordion('collapsehttp01')");
        executeScript("getData('/api/protocols/http-01/plugins/replay-plugin/start','GET',()=>reloadProtocolmysql01().then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Start replaying with fake backend");
        switchToTab("main");

        alertWhenHumanDriven("Starting the replaying without backend");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Cleaning cache and cookies");
        cleanBrowserCache();
        Sleeper.sleep(1000);
        B_testNavigation();
        alertWhenHumanDriven("No Backend Replaying completed");
        switchToTab("tpm");
        executeScript("getData('/api/protocols/http-01/plugins/replay-plugin/stop','GET',()=>reloadProtocolmysql01().then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Test completed");
    }

    private void replayWithoutMysql() throws Exception {
        stopContainer("net-core-mysql");
        alertWhenHumanDriven("Stopped mysql container");
        Sleeper.sleep(1000);
        scrollFind("mysql01panel");
        executeScript("closeAccordion('collapseWildcard')");
        executeScript("opneAccordion('collapsemysql01')");
        executeScript("getData('/api/protocols/mysql-01/plugins/replay-plugin/start','GET',()=>reloadProtocolmysql01().then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Start replaying with fake mysql");
        switchToTab("main");

        Sleeper.sleep(1000);
        alertWhenHumanDriven("Cleaning cache and cookies");
        cleanBrowserCache();
        Sleeper.sleep(1000);
        B_testNavigation();
        alertWhenHumanDriven("No MySql Replaying completed");
        switchToTab("tpm");
        executeScript("getData('/api/protocols/mysql-01/plugins/replay-plugin/stop','GET',()=>reloadProtocolmysql01().then(()=>reloadWildcard()).then(()=>reloadActive()))");
        Sleeper.sleep(1000);
        alertWhenHumanDriven("Stopped mysql replaying");
    }

    private void recordingData() throws InterruptedException {
        cleanBrowserCache();
        newTab("tpm");
        alertWhenHumanDriven("Starting the recording");
        Sleeper.sleep(1000);
        //Open tpm
        navigateTo("http://net-core-tpm:8081/plugins");
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
}
