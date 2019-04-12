package at.madlmayr.tools;

import at.madlmayr.ToolCallRequest;
import at.madlmayr.slack.SlackMember;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Starts up a DynamoDB server using the command line launcher and returns an AmazonDynamoDB client that
 * can connect to it via a local network port.
 *
 * @author msgroi
 * @link https://github.com/salesforce/mt-dynamo/blob/master/src/test/java/com/salesforce/dynamodbv2/dynamodblocal/LocalDynamoDbServer.java
 */

public class LocalDynamoDbServer {


    private static final Logger LOGGER = LogManager.getLogger(LocalDynamoDbServer.class);
    private final int port;
    private DynamoDBProxyServer server;
    private boolean running;

    public LocalDynamoDbServer() {
        this.port = getRandomPort();
    }

    /*
     * Creates an instance with the specified port.
     */
    public LocalDynamoDbServer(int port) {
        this.port = port;
    }

    /**
     * Find a random open port and returns it.
     */
    public static int getRandomPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * If it's not already running, starts the server, then returns a client regardless.
     */
    public AmazonDynamoDB start() {
        if (!running) {
            try {
                System.setProperty("sqlite4java.library.path", "native-libs");
                server = ServerRunner
                        .createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", String.valueOf(port)});
                server.start();
                running = true;
                LOGGER.info("started dynamodblocal on port " + port);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return getClient();
    }

    /**
     * If the server is running, stops the server.  Returns silently otherwise.
     */
    public void stop() {
        if (running) {
            try {
                server.stop();
                running = false;
                LOGGER.info("stopped dynamodblocal on port " + port);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Returns the port of the server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns a client that can be used to connect to the server.
     */
    public AmazonDynamoDB getClient() {
        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, Regions.EU_CENTRAL_1.getName())).build();
    }


    public void createAccountTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        SlackMember.COLUMN_COMPANY_TOOL, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        SlackMember.COLUMN_ID, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(SlackMember.COLUMN_COMPANY_TOOL, KeyType.HASH), new KeySchemaElement(SlackMember.COLUMN_ID, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(SlackMember.TABLE_NAME);
        createTable(request);
    }


    public void createConfigTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallRequest.COLUMN_COMPANY, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallRequest.COLUMN_TOOL, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(ToolCallRequest.COLUMN_COMPANY, KeyType.HASH), new KeySchemaElement(ToolCallRequest.COLUMN_TOOL, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(ToolCallRequest.TABLE_NAME);
        createTable(request);
    }

    private void createTable(CreateTableRequest request) {

        final AmazonDynamoDB ddb = getClient();
        CreateTableResult result = ddb.createTable(request);
        LOGGER.info("Table '{}' created", result.getTableDescription().getTableName());
    }


    public void deleteAccountTable() {
        final AmazonDynamoDB ddb = getClient();
        DeleteTableResult result = ddb.deleteTable(SlackMember.TABLE_NAME);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }


    public void deleteTable() {
        final AmazonDynamoDB ddb = getClient();
        DeleteTableResult result = ddb.deleteTable(ToolCallRequest.TABLE_NAME);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }

}
