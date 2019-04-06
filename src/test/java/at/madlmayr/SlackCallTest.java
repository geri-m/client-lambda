package at.madlmayr;

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
    public void exampleTest() throws Exception {
        WireMock.reset();
        LOGGER.debug("Port: {}", wireMockServer.port());
        // WireMock.reset();
        stubFor(get(urlEqualTo("/api/users.list/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody("{\n" +
                                "    \"ok\": true,\n" +
                                "    \"members\": [\n" +
                                "        {\n" +
                                "            \"id\": \"xxxx\",\n" +
                                "            \"team_id\": \"yyyyy\",\n" +
                                "            \"name\": \"zzzz\",\n" +
                                "            \"deleted\": false,\n" +
                                "            \"color\": \"9f69e7\",\n" +
                                "            \"real_name\": \"Gerald Madlmayr\",\n" +
                                "            \"tz\": \"Europe/Amsterdam\",\n" +
                                "            \"tz_label\": \"Central European Summer Time\",\n" +
                                "            \"tz_offset\": 7200,\n" +
                                "            \"profile\": {\n" +
                                "                \"title\": \"Java can be written in any Language\",\n" +
                                "                \"phone\": \"+49-160-4798987\",\n" +
                                "                \"skype\": \"\",\n" +
                                "                \"real_name\": \"Gerald Madlmayr\",\n" +
                                "                \"real_name_normalized\": \"Gerald Madlmayr\",\n" +
                                "                \"display_name\": \"mad0001g\",\n" +
                                "                \"display_name_normalized\": \"mad0001g\",\n" +
                                "                \"status_text\": \"\",\n" +
                                "                \"status_emoji\": \":facepunch:\",\n" +
                                "                \"status_expiration\": 0,\n" +
                                "                \"avatar_hash\": \"ca6e9c7dd999\",\n" +
                                "                \"image_original\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_original.jpg\",\n" +
                                "                \"email\": \"gerald.madlmayr@gmx.com\",\n" +
                                "                \"first_name\": \"Gerald\",\n" +
                                "                \"last_name\": \"Madlmayr\",\n" +
                                "                \"image_24\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_24.jpg\",\n" +
                                "                \"image_32\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_32.jpg\",\n" +
                                "                \"image_48\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_48.jpg\",\n" +
                                "                \"image_72\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_72.jpg\",\n" +
                                "                \"image_192\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_192.jpg\",\n" +
                                "                \"image_512\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_512.jpg\",\n" +
                                "                \"image_1024\": \"https://avatars.slack-edge.com/2018-01-08/296553297846_ca6e9c7dd999129d4aa4_1024.jpg\",\n" +
                                "                \"status_text_canonical\": \"\",\n" +
                                "                \"team\": \"T8F0BHJ6T\",\n" +
                                "                \"is_custom_image\": true\n" +
                                "            },\n" +
                                "            \"is_admin\": true,\n" +
                                "            \"is_owner\": true,\n" +
                                "            \"is_primary_owner\": true,\n" +
                                "            \"is_restricted\": false,\n" +
                                "            \"is_ultra_restricted\": false,\n" +
                                "            \"is_bot\": false,\n" +
                                "            \"is_app_user\": false,\n" +
                                "            \"updated\": 1549734316\n" +
                                "        }]}")));

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.SLACK.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/api/users.list/"}, 1L);
        RequestStreamHandler call = new SlackCall();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        ToolCallResult resultFromCall = mapper.readValue(outputStream.toString(), ToolCallResult.class);
        assertThat(resultFromCall.getAmountOfUsers() == 1);

        /*
        verify(postRequestedFor(urlMatching("/my/resource/[a-z0-9]+"))
                .withRequestBody(matching(".*<message>1234</message>.*"))
                .withHeader("Content-Type", notMatching("application/json")));

         */
    }
}
