package ch.hslu.vsk.logger.server.viewer;

import ch.hslu.vsk.logger.api.LogLevel;
import ch.hslu.vsk.logger.api.Logger;
import ch.hslu.vsk.logger.api.LoggerSetupFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.glassfish.grizzly.Grizzly;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.apache.logging.log4j.core.LoggerContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Testcontainers
class LoggerViewerClientIT {

    private static final int LOGGER_SERVER_PORT = 50000;
    private static final int LOGGER_VIEWER_SERVER_PORT = 8025;
    private List<GenericContainer<?>> createdContainers = new ArrayList<>();
    private static TestAppender testAppendable;

    private GenericContainer<?> createServerContainer(int loggerServerPort, int loggerViewerServerPort) {

        try(var container = new GenericContainer<>(DockerImageName.parse("g05/loggerserver:latest"))
                .withCommand("--listen-port=" + loggerServerPort)
                .withExposedPorts(loggerServerPort, loggerViewerServerPort)
                .withStartupTimeout(Duration.ofSeconds(10))
                .waitingFor(Wait.forListeningPort());
        ) {
            List<String> portBindings = new ArrayList<>();
            portBindings.add(loggerServerPort + ":" + loggerServerPort);
            portBindings.add(loggerViewerServerPort + ":" + loggerViewerServerPort);
            container.setPortBindings(portBindings);
            this.createdContainers.add(container);
            return container;
        }


    }

    //setup the appender to capture the logs
    @BeforeAll
     static void setupAppender() {
        testAppendable = TestAppender.createAppender();
        testAppendable.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        LoggerConfig loggerConfig = context.getConfiguration().getLoggerConfig(LoggerViewerClient.class.getName());
        //add the appender to the Specific LoggerConfig
        loggerConfig.addAppender(testAppendable, null, null);
        context.updateLoggers();
    }

    @AfterEach
     void afterEach() {
        for (GenericContainer<?> createdContainer : this.createdContainers) {
            createdContainer.close();
        }
        this.createdContainers.clear();
        testAppendable.clear();
    }
    @Test
     void testLoggerViewerClientConnects() throws InterruptedException {
        var server = createServerContainer(LOGGER_SERVER_PORT, LOGGER_VIEWER_SERVER_PORT);
        try {
            server.start();
        } catch (ContainerLaunchException e) {
            System.err.println("Container failed to start: " + e.getMessage());
            System.err.println("Container logs: " + server.getLogs());
            return;
        }

        //container logs are not printed to the console
        System.out.println(server.getLogs());

        //create logger Client
        Logger logger = LoggerSetupFactory.provider("ch.hslu.vsk.logger.component.LoggerFactory")
                .getLogger(server.getHost() + ":" + server.getMappedPort(LOGGER_SERVER_PORT), "DemoApp");

        // Connect LoggerViewerClient to LoggerViewerServer
        LoggerViewerClient viewerClient = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));

        //viewerClient must use this connection to connect to the LoggerViewerServer
        new Thread(viewerClient::start).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        Assertions.assertTrue(viewerClient.isConnected());
        for (int i = 0; i < 10; i++) {
            logger.log(LogLevel.INFO, "Test " + i);
        }

        Thread.sleep(1000);
        //check if messages are received
        String logMessages = testAppendable.getOutput();
        System.out.println("appendables: " + logMessages);
        Thread.sleep(1000);

        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(logMessages.contains("Test " + i));
            System.out.println("Found Message " + i);
        }

        //stop the server and the viewerClient
        viewerClient.stop();
        server.stop();
    }

    @Test
     void testCapacityOfMessagesPerTime() throws InterruptedException {
        var server = createServerContainer(LOGGER_SERVER_PORT, LOGGER_VIEWER_SERVER_PORT);
        try {
            server.start();
        } catch (ContainerLaunchException e) {
            System.err.println("Container failed to start: " + e.getMessage());
            System.err.println("Container logs: " + server.getLogs());
            return;
        }

        //container logs are not printed to the console
        System.out.println(server.getLogs());

        //create logger Client
        Logger logger = LoggerSetupFactory.provider("ch.hslu.vsk.logger.component.LoggerFactory")
                .getLogger(server.getHost() + ":" + server.getMappedPort(LOGGER_SERVER_PORT), "DemoApp");



        // Connect LoggerViewerClient to LoggerViewerServer
        LoggerViewerClient viewerClient = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));

        //viewerClient must use this connection to connect to the LoggerViewerServer
        new Thread(viewerClient::start).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        Assertions.assertTrue(viewerClient.isConnected());

        //send messages during 3 seconds to the server

        long beginningTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - beginningTime < 1000) {
            for (int i = 0; i < 10; i++) {
                logger.log(LogLevel.INFO, "Test " + i);
            }
        }
        //wait one second to receive the messages
        Thread.sleep(1000);
        //count the number of messages received
        String logMessages = testAppendable.getOutput();
        String[] messages = logMessages.split("\n");
        Assertions.assertTrue(messages.length  > 100);


        server.stop();
        viewerClient.stop();
        System.out.println("Number of messages received in 1 second: " + messages.length);
    }

    @Test
     void testLoggerViewerDisconnect(){
        var server = createServerContainer(LOGGER_SERVER_PORT, LOGGER_VIEWER_SERVER_PORT);
        try {
            server.start();
        } catch (ContainerLaunchException e) {
            System.err.println("Container failed to start: " + e.getMessage());
            System.err.println("Container logs: " + server.getLogs());
            return;
        }

        //container logs are not printed to the console
        System.out.println(server.getLogs());

        // Connect LoggerViewerClient to LoggerViewerServer
        LoggerViewerClient viewerClient = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));

        //viewerClient must use this connection to connect to the LoggerViewerServer
        new Thread(viewerClient::start).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        Assertions.assertTrue(viewerClient.isConnected());
        viewerClient.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to stop");
        }
        Assertions.assertFalse(viewerClient.isConnected());
        server.stop();
    }

    @Test
    public void testTwoLoggerViewerConnected() throws InterruptedException {
        var server = createServerContainer(LOGGER_SERVER_PORT, LOGGER_VIEWER_SERVER_PORT);
        try {
            server.start();
        } catch (ContainerLaunchException e) {
            System.err.println("Container failed to start: " + e.getMessage());
            System.err.println("Container logs: " + server.getLogs());
            return;
        }

        //container logs are not printed to the console
        System.out.println(server.getLogs());

        //create logger Client
        Logger logger = LoggerSetupFactory.provider("ch.hslu.vsk.logger.component.LoggerFactory")
                .getLogger(server.getHost() + ":" + server.getMappedPort(LOGGER_SERVER_PORT), "DemoApp");

        // Connect LoggerViewerClient to LoggerViewerServer
        LoggerViewerClient viewerClient = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));
        LoggerViewerClient viewerClient2 = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));

        //viewerClient must use this connection to connect to the LoggerViewerServer
        new Thread(viewerClient::start).start();
        new Thread(viewerClient2::start).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        Assertions.assertTrue(viewerClient.isConnected());
        Assertions.assertTrue(viewerClient2.isConnected());
        for (int i = 0; i < 10; i++) {
            logger.log(LogLevel.INFO, "Test " + i);
        }

        Thread.sleep(1000);
        //check if messages are received
        String logMessages = testAppendable.getOutput();
        System.out.println("appendables: " + logMessages);
        Thread.sleep(1000);

        for (int i = 0; i < 10; i++) {
            //check if the messages are received by both clients, so the message should be in the logMessages twice
            Assertions.assertTrue(logMessages.contains("Test " + i));
            Assertions.assertTrue(logMessages.indexOf("Test " + i) != logMessages.lastIndexOf("Test " + i), "Message not received by both clients: Test " + i);
            System.out.println("Found Message " + i);
        }

        //stop the server and the viewerClient
        viewerClient.stop();
        viewerClient2.stop();
        server.stop();
    }

    /**
     * versichert dass System stabil läuft wenn zwei LoggerClients verbunden sind
     */
    @Test
    public void testTwoLoggerClientConnected(){
        var server = createServerContainer(LOGGER_SERVER_PORT, LOGGER_VIEWER_SERVER_PORT);
        try {
            server.start();
        } catch (ContainerLaunchException e) {
            System.err.println("Container failed to start: " + e.getMessage());
            System.err.println("Container logs: " + server.getLogs());
            return;
        }

        //container logs are not printed to the console
        System.out.println(server.getLogs());

        //create logger Client
        Logger logger = LoggerSetupFactory.provider("ch.hslu.vsk.logger.component.LoggerFactory")
                .getLogger(server.getHost() + ":" + server.getMappedPort(LOGGER_SERVER_PORT), "DemoApp");
        Logger logger2 = LoggerSetupFactory.provider("ch.hslu.vsk.logger.component.LoggerFactory")
                .getLogger(server.getHost() + ":" + server.getMappedPort(LOGGER_SERVER_PORT), "DemoApp2");

        // Connect LoggerViewerClient to LoggerViewerServer
        LoggerViewerClient viewerClient = new LoggerViewerClient(server.getHost(), server.getMappedPort(LOGGER_VIEWER_SERVER_PORT));

        //viewerClient must use this connection to connect to the LoggerViewerServer
        new Thread(viewerClient::start).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        Assertions.assertTrue(viewerClient.isConnected());
        for (int i = 0; i < 10; i++) {
            logger.log(LogLevel.INFO, "Test1 " + i);
            logger2.log(LogLevel.INFO, "Test2 " + i);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }
        //check if messages are received
        String logMessages = testAppendable.getOutput();
        System.out.println("appendables: " + logMessages);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting for the viewerClient to connect");
        }

        for (int i = 0; i < 10; i++) {
            Assertions.assertTrue(logMessages.contains("Test1 " + i));
            Assertions.assertTrue(logMessages.contains("Test2 " + i));
            System.out.println("Found Message " + i);
        }

        //stop the server and the viewerClient
        viewerClient.stop();
        server.stop();
    }

}