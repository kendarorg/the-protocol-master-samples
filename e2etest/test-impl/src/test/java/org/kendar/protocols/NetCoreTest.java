package org.kendar.protocols;

import org.junit.jupiter.api.*;
import org.kendar.protocol.utils.Sleeper;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class NetCoreTest extends BasicTest{
    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        tearDownAfterClassBase();
    }
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        setupDirectories("net-core");
        var exposed = setupContainer("net-core-tpm");
        //TPM
        exposed.withExposedService("net-core-tpm-mysql", 23306);

        //MySQL server
        exposed.withExposedService("net-core-mysql-mysql", 13306);
        exposed.waitingFor("net-core-mysql-mysql",
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(5)));

        //Http server
        exposed.withExposedService("net-core-http-http", 9080);
        exposed.waitingFor("net-core-http-http",
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(5)));

        exposed.withExposedService("net-core-rest-http", 9081);
        exposed.waitingFor("net-core-rest-http",
                Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(5)));
        exposed.start();
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
    void testNetCore() throws Exception {
        getNavigation().navigateTo("http://net-core-tpm:8081/api/status");
        Sleeper.sleep(200,()-> getDriver().getPageSource().contains("OK"),"Unreachable http://net-core-tpm:8081/api/status");
        getNavigation().navigateTo("http://net-core-http/api/status");
        Sleeper.sleep(200,()-> getDriver().getPageSource().contains("OK"),"Unreachable http://net-core-http/api/status");
        getNavigation().navigateTo("http://net-core-rest/api/status");
        Sleeper.sleep(200,()-> getDriver().getPageSource().contains("OK"),"Unreachable http://net-core-rest/api/status");
    }
}
