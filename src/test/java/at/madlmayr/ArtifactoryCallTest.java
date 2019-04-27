package at.madlmayr;

import at.madlmayr.artifactory.ArtifactoryCall;
import at.madlmayr.artifactory.ArtifactoryListElement;
import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.tools.ArtifactoryListElementWithUser;
import at.madlmayr.tools.FileUtils;
import at.madlmayr.tools.LocalDynamoDbServer;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

public class ArtifactoryCallTest {

    // @Rule
    // public static WireMockRule wireMockServer = new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LogManager.getLogger(LambdaTest.class);
    // TODO: Check if @Rule would be more suitable for handling the tests
    private static WireMockServer wireMockServer;
    private static LocalDynamoDbServer localDynamoDbServer;

    @BeforeAll
    public static void beforeAll() {
        // handle issues, in case segments are not there and disable therefore xray.
        CallUtils.disableXray();
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort()); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        localDynamoDbServer = new LocalDynamoDbServer();
        localDynamoDbServer.start();
        LOGGER.debug("Wiremock: {}", wireMockServer.port());
        LOGGER.debug("DynamoDB: {}", localDynamoDbServer.getPort());
        localDynamoDbServer.createAccountTable();
        localDynamoDbServer.createCallResultTable();
    }

    @AfterAll
    public static void afterAll() {
        localDynamoDbServer.deleteAccountTable();
        localDynamoDbServer.deleteCallResultTable();
        localDynamoDbServer.stop();
        wireMockServer.stop();
    }


    public static List<ArtifactoryListElementWithUser> initWiremock(String filename, int wiremockPort) throws Exception {
        String response = FileUtils.readFromFile(filename);
        ObjectMapper localMapper = new ObjectMapper();

        List<ArtifactoryListElement> list = new ArrayList<>();
        List<ArtifactoryListElementWithUser> responseFromFile = localMapper.readValue(response, new TypeReference<List<ArtifactoryListElementWithUser>>() {
        });
        for (ArtifactoryListElementWithUser user : responseFromFile) {
            URL originalUrl = new URL(user.getListElement().getUri());
            URL newUrl = new URL(originalUrl.getProtocol(), originalUrl.getHost(), wiremockPort, originalUrl.getFile());
            user.getListElement().setUri(newUrl.toString());

            // put each user into wiremock
            stubFor(get(urlEqualTo(newUrl.getPath()))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json; charset=utf-8")
                            .withBody(localMapper.writeValueAsString(user.getUser()))));

            list.add(user.getListElement());
        }

        // Put the whole user List into Wiremock
        stubFor(get(urlEqualTo("/gma/api/security/users"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withBody(localMapper.writeValueAsString(list))));

        return responseFromFile;
    }

    @Test
    public void userListTest() throws Exception {
        WireMock.reset();

        Set<String> memberIds = new HashSet<>();
        for (ArtifactoryListElementWithUser user : initWiremock("/artifactory_01.json", wireMockServer.port())) {
            memberIds.add(user.getUser().getName());
        }

        ToolCallRequest slack = new ToolCallRequest(new String[]{"gma", ToolEnum.ARTIFACTORY.getName(), "sometoken", "http://localhost:" + wireMockServer.port() + "/gma/api/security/users"}, 1L, 1);
        RequestStreamHandler call = new ArtifactoryCall(localDynamoDbServer.getPort());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream targetStream = new ByteArrayInputStream(new JSONObject(slack).toString().getBytes());
        call.handleRequest(targetStream, outputStream, null);

        List<ToolCallResult> resultList = localDynamoDbServer.getAllToolCallResult("gma", ToolEnum.ARTIFACTORY, 1L);
        assertThat(resultList.size()).isEqualTo(1);
        assertThat(resultList.get(0).getAmountOfUsers()).isEqualTo(103);

        List<ArtifactoryUser> artifactoryUserList = localDynamoDbServer.getArtifactoryUserListByCompanyToolTimestamp("gma#" + ToolEnum.ARTIFACTORY.getName() + "#1970-01-01T00:00:00.001Z");

        for (ArtifactoryUser artifactoryUser : artifactoryUserList) {
            assertThat(memberIds.contains(artifactoryUser.getName()));
            memberIds.remove(artifactoryUser.getName());
        }

        assertThat(memberIds.size()).isEqualTo(0);
        assertThat(artifactoryUserList.size()).isEqualTo(103);

    }

}
