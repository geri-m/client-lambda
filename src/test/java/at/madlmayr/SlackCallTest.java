package at.madlmayr;

import at.madlmayr.slack.SlackCall;
import at.madlmayr.tools.FileUtils;
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

    @BeforeAll
    public static void beforeAll() {
        // handle issues, in case segments are not there and disable therefore xray.
        AWSXRay.getGlobalRecorder().setContextMissingStrategy((s, aClass) -> LOGGER.warn("Context for XRay  is unset for Testing"));
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    public static void afterAll() {
        wireMockServer.stop();
    }

    @Test
    public void userListTest() throws Exception {
        WireMock.reset();
        LOGGER.debug("Port: {}", wireMockServer.port());
        String response = FileUtils.readFromFile("/slackdata_01.json");
        // WireMock.reset();
        stubFor(get(urlEqualTo("/api/users.list/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(response)));

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.SLACK.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L);
        RequestStreamHandler call = new SlackCall();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers()).isEqualTo(163);
    }
}
