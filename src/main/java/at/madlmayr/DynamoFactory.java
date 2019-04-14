package at.madlmayr;

import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.slack.SlackMember;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class DynamoFactory {

    public DynamoAbstraction create() {
        return new DynamoAbstraction();
    }

    public DynamoAbstraction create(int port) {
        return new DynamoAbstraction(port);
    }

    public static class DynamoAbstraction {

        private static final Logger LOGGER = LogManager.getLogger(DynamoFactory.class);
        private int port = 0;

        private DynamoAbstraction() {

        }

        public DynamoAbstraction(int port) {
            this.port = port;
        }

        public AmazonDynamoDB getClient() {
            // this is not so cool, as there is an "if" in each getClient call..
            if (port != 0) {
                return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, Regions.EU_CENTRAL_1.getName())).build();
            } else {
                return AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
            }
        }

        public void writeSlackMember(final SlackMember member) {
            DynamoDBMapper mapper = new DynamoDBMapper(getClient());
            mapper.save(member);
        }

        public void writeArtifactoryUser(final ArtifactoryUser member) {
            DynamoDBMapper mapper = new DynamoDBMapper(getClient());
            mapper.save(member);
        }

        public void writeJiraUser(final JiraSearchResultElement member) {
            DynamoDBMapper mapper = new DynamoDBMapper(getClient());
            mapper.save(member);
        }

        public void writeCallResult(final ToolCallResult result) {
            DynamoDBMapper mapper = new DynamoDBMapper(getClient());
            mapper.save(result);
        }

        public List<ToolCallRequest> getAllToolCallRequest() {

            DynamoDBMapper mapper = new DynamoDBMapper(getClient());
            List<ToolCallRequest> r = mapper.scan(ToolCallRequest.class, new DynamoDBScanExpression());

            // Get all Element from the Table
            /*
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(ToolCallRequest.TABLE_NAME);

            ScanResult result = dynamo.scan(scanRequest);
            */
            return r;
        }
    }
}
