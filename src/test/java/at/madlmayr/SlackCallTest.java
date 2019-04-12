package at.madlmayr;

import at.madlmayr.slack.SlackCall;
import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import at.madlmayr.tools.FileUtils;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.xray.AWSXRay;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class SlackCallTest {

    // @Rule
    // public static WireMockRule wireMockServer = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    // TODO: Check if @Rule would be more suitable for handling the tests
    private static WireMockServer wireMockServer;
    private static DynamoDBProxyServer server;

    @BeforeAll
    public static void beforeAll() throws Exception {
        // handle issues, in case segments are not there and disable therefore xray.
        AWSXRay.getGlobalRecorder().setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay is unset for Testing"));
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        System.setProperty("sqlite4java.library.path", "native-libs");
        String port = "8000";
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", port});
        server.start();
        createRawDataTable();

    }

    private static AmazonDynamoDB getConnectionLocalhost() {
        return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("http://localhost:8000", Regions.EU_CENTRAL_1.getName()))
                .build();
    }

    private static void createRawDataTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(
                        SlackMember.COLUMN_COMPANY_TOOL, ScalarAttributeType.S))
                .withAttributeDefinitions(new AttributeDefinition(
                        SlackMember.COLUMN_ID, ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(SlackMember.COLUMN_COMPANY_TOOL, KeyType.HASH), new KeySchemaElement(SlackMember.COLUMN_ID, KeyType.RANGE))
                .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                .withTableName(SlackMember.TABLE_NAME);

        final AmazonDynamoDB ddb = getConnectionLocalhost();
        CreateTableResult result = ddb.createTable(request);
        LOGGER.info("Table '{}' created", result.getTableDescription().getTableName());
    }

    private static void deleteTable() {
        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DeleteTableResult result = ddb.deleteTable(SlackMember.TABLE_NAME);
        LOGGER.info("Table '{}' deleted", result.getTableDescription().getTableName());
    }

    @AfterAll
    public static void afterAll() {
        deleteTable();
        wireMockServer.stop();
    }

    @Test
    public void userListTest() throws Exception {
        WireMock.reset();
        LOGGER.debug("Port: {}", wireMockServer.port());
        String response = FileUtils.readFromFile("/slackdata_01.json");

        List<String> memberIds = new ArrayList<>();
        SlackResponse responseFromFile = mapper.readValue(response, SlackResponse.class);
        for (SlackMember m : responseFromFile.getMembers()) {
            memberIds.add(m.getId());
        }


        // WireMock.reset();
        stubFor(get(urlEqualTo("/api/users.list/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(response)));

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.SLACK.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L);
        RequestStreamHandler call = new SlackCall(new URL("http://localhost:8000"));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers()).isEqualTo(163);

        final AmazonDynamoDB ddb = getConnectionLocalhost();
        DynamoDBMapper dbMapper = new DynamoDBMapper(ddb);
        SlackMember query = new SlackMember();
        query.setCompanyToolTimestamp("gma#" + ToolEnum.SLACK.getName() + "#" + Utils.standardTimeFormat(1L));
        DynamoDBQueryExpression<SlackMember> queryExpression = new DynamoDBQueryExpression<SlackMember>()
                .withHashKeyValues(query);

        List<SlackMember> itemList = dbMapper.query(SlackMember.class, queryExpression);

        for (SlackMember m : itemList) {
            assertThat(memberIds.contains(m.getId()));
            memberIds.remove(m.getId());
        }

        assertThat(memberIds.size()).isEqualTo(0);
        assertThat(itemList.size()).isEqualTo(163);
    }
}
