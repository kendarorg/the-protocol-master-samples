package org.kendar.protocols;

import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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
        tearDownAfterEachBase();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        beforeEachBase(testInfo);
    }

    @Test
    void testDockerIsUp() throws Exception {
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

    @Test
    void testNavigation() {
        navigateTo("http://net-core-http/index.html");

        //Insert item
        fillItem("taskName", "Laundry");
        selectItem("addPriority", "High");
        fillItem("notes", "Wash Cotton");

        //Submit
        clickItem("submitNewTask");
        Sleeper.sleep(1000);

        //Find the selected item
        var tableBody = findElementById("listTableBody");
        var tr = tableBody.findElements(By.xpath(".//tr")).get(0);
        tr.findElement(By.xpath(".//td[contains(text(), \"Laundry\")]"));
        tr.findElement(By.xpath(".//td[contains(text(), \"Wash Cotton\")]"));

        //Modify the status
        var status = tr.findElements(By.xpath(".//select")).get(1);
        selectItem(status, "Completed");

        //Update
        var button = tr.findElements(By.xpath(".//button")).get(1);
        button.click();
        Sleeper.sleep(1000);

        //Reload the stale element
        tableBody = findElementById("listTableBody");
        tr = tableBody.findElements(By.xpath(".//tr")).get(0);
        button = tr.findElements(By.xpath(".//button")).get(1);
        button.click();
        Sleeper.sleep(1000);

        //Set the archive
        clickItem("setArchivedTasks");
        Sleeper.sleep(1000);
    }
}
