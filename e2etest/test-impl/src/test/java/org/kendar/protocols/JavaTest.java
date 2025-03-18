package org.kendar.protocols;
import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;

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
    void B_testNavigation() throws InterruptedException {
        navigateTo("about:blank");
        Sleeper.sleep(500);
        navigateTo("http://java-rest/index.html");//itemUpdateMETA
        Sleeper.sleep(6000);

//        alertWhenHumanDriven("Inserting new task");
//        //Insert item
//        fillItem("taskName", "Laundry");
//        selectItem("addPriority", "High");
//        fillItem("notes", "Wash Cotton");
//
//        //Submit
//        clickItem("submitNewTask");
//        Sleeper.sleep(1000);
//        takeSnapshot();
//        alertWhenHumanDriven("Inserting new task");
//
//        //Find the selected item
//
//        assertNotNull(findElementByXPath("//tbody[@id='listTableBody']/tr/td[contains(text(), \"Laundry\")]"));
//        assertNotNull(findElementByXPath("//tbody[@id='listTableBody']/tr/td[contains(text(), \"Wash Cotton\")]"));
//        takeSnapshot();
//
//
//
//        alertWhenHumanDriven("Completing task");
//        //Modify the status
//        var status = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/select");
//        selectItem(status.get(1), "Completed");
//        Sleeper.sleep(1000);
//        takeSnapshot();
//
//
//        //Update
//        var button = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/button").get(1);
//        button.click();
//        Sleeper.sleep(1000);
//        takeSnapshot();
//
//
//        alertWhenHumanDriven("Archiving the task");
//        //Reload the stale element
//
//        button = findElementsByXPath("//tbody[@id='listTableBody']/tr/td/button").get(1);
//        button.click();
//        Sleeper.sleep(1000);
//        takeSnapshot();
//
//        //Set the archive
//
//        clickItem("setArchivedTasks");
//        Sleeper.sleep(2000);
//        takeSnapshot();
//
//
//        alertWhenHumanDriven("Clean up the task");
//        //Delete old item
//
//        button = findElementByXPath("//tbody[@id='archivedTableBody']/tr/td/button");
//        button.click();
//        Sleeper.sleep(1000);
//        var yesButton = findElementByXPath(".//button[contains(text(), \"Yes, delete it!\")]");
//        yesButton.click();
//        Sleeper.sleep(1000);
//
//        var okButton = findElementByXPath(".//button[contains(text(), \"OK\")]");
//        okButton.click();
//        Sleeper.sleep(1000);
//
//        alertWhenHumanDriven("Operation completed");
//        navigateTo("about:blank");
    }

}
