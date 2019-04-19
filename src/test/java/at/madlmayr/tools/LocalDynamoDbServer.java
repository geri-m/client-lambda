package at.madlmayr.tools;

import at.madlmayr.*;
import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.slack.SlackMember;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

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
    private final DynamoFactory.DynamoAbstraction db;

    public LocalDynamoDbServer() {
        this.port = getRandomPort();
        db = new DynamoFactory().create(port);
    }

    /*
     * Creates an instance with the specified port.
     */
    public LocalDynamoDbServer(int port) {
        this.port = port;
        db = new DynamoFactory().create(port);
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
    public void start() {
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

    public void createAccountTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        Account.COLUMN_COMPANY_TOOL, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        Account.COLUMN_ID, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(Account.COLUMN_COMPANY_TOOL, KeyType.HASH), new KeySchemaElement(Account.COLUMN_ID, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(Account.TABLE_NAME);
        db.createTable(request);
    }


    public void createCallResultTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallResult.COLUMN_COMPANY_TOOL, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        ToolCallResult.TIME_STAMP, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(ToolCallResult.COLUMN_COMPANY_TOOL, KeyType.HASH), new KeySchemaElement(ToolCallResult.TIME_STAMP, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(ToolCallResult.TABLE_NAME);
        db.createTable(request);
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
        db.createTable(request);
    }

    public void insertConfig(final List<ToolCallRequest> calls) {
        DynamoDBMapper mapper = db.getMapper();
        for (ToolCallRequest call : calls) {
            mapper.save(call);
        }
    }



    public List<SlackMember> getSlackMemberListByCompanyToolTimestamp(final String companyToolTimestamp) {
        DynamoDBMapper mapper = db.getMapper();
        SlackMember query = new SlackMember();
        query.setCompanyToolTimestamp(companyToolTimestamp);
        DynamoDBQueryExpression<SlackMember> queryExpression = new DynamoDBQueryExpression<SlackMember>()
                .withHashKeyValues(query);

        return mapper.query(SlackMember.class, queryExpression);
    }

    public List<ArtifactoryUser> getArtifactoryUserListByCompanyToolTimestamp(final String companyToolTimestamp) {
        DynamoDBMapper mapper = db.getMapper();
        ArtifactoryUser query = new ArtifactoryUser();
        query.setCompanyToolTimestamp(companyToolTimestamp);
        DynamoDBQueryExpression<ArtifactoryUser> queryExpression = new DynamoDBQueryExpression<ArtifactoryUser>()
                .withHashKeyValues(query);

        return mapper.query(ArtifactoryUser.class, queryExpression);
    }


    public List<JiraSearchResultElement> getJiraUserListByCompanyToolTimestamp(final String companyToolTimestamp) {
        DynamoDBMapper mapper = db.getMapper();
        JiraSearchResultElement query = new JiraSearchResultElement();
        query.setCompanyToolTimestamp(companyToolTimestamp);
        DynamoDBQueryExpression<JiraSearchResultElement> queryExpression = new DynamoDBQueryExpression<JiraSearchResultElement>()
                .withHashKeyValues(query);

        return mapper.query(JiraSearchResultElement.class, queryExpression);
    }

    public List<ToolCallRequest> getToolCallRequests(final String companyTool) {
        DynamoDBMapper mapper = db.getMapper();
        ToolCallRequest query = new ToolCallRequest();
        query.setCompany(companyTool);

        DynamoDBQueryExpression<ToolCallRequest> queryExpression = new DynamoDBQueryExpression<ToolCallRequest>()
                .withHashKeyValues(query);

        return mapper.query(ToolCallRequest.class, queryExpression);
    }


    public List<ToolCallResult> getAllToolCallResult(final String company, final ToolEnum tool, final long batchTimeStamp) {
        DynamoDBMapper mapper = db.getMapper();
        ToolCallResult query = new ToolCallResult();
        query.setCompany(company);
        query.setTimeStamp(batchTimeStamp);
        query.setTool(tool.getName());

        DynamoDBQueryExpression<ToolCallResult> queryExpression = new DynamoDBQueryExpression<ToolCallResult>()
                .withHashKeyValues(query);

        return mapper.query(ToolCallResult.class, queryExpression);
    }

    public List<ToolCallResult> getAllToolCallResult() {
        DynamoDBMapper mapper = db.getMapper();
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        return mapper.scan(ToolCallResult.class, scanExpression);
    }


    public void deleteAccountTable() {
        DeleteTableRequest r = new DeleteTableRequest();
        r.setTableName(Account.TABLE_NAME);
        DeleteTableResult result = db.deleteTable(r);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }


    public void deleteConfigTable() {
        DeleteTableRequest r = new DeleteTableRequest();
        r.setTableName(ToolCallRequest.TABLE_NAME);
        DeleteTableResult result = db.deleteTable(r);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }

    public void deleteCallResultTable() {
        DeleteTableRequest r = new DeleteTableRequest();
        r.setTableName(ToolCallResult.TABLE_NAME);
        DeleteTableResult result = db.deleteTable(r);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }

}
