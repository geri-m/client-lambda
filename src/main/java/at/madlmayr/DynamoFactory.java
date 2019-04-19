package at.madlmayr;

import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.slack.SlackMember;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableResult;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
        private final AmazonDynamoDB db;
        private final DynamoDBMapper mapper;


        private DynamoAbstraction() {
            db = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
            mapper = new DynamoDBMapper(db);
        }

        public DynamoAbstraction(int port) {
            db = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, Regions.EU_CENTRAL_1.getName())).build();
            mapper = new DynamoDBMapper(db);
        }

        public DynamoDBMapper getMapper() {
            return mapper;
        }

        public void writeSlackMembersBatch(final List<SlackMember> members) {
            mapper.batchWrite(members, new ArrayList<>());
        }

        public void writeArtifactoryMembersBatch(final List<ArtifactoryUser> members) {
            mapper.batchWrite(members, new ArrayList<>());
        }

        public void writeJiraMembersBatch(final List<JiraSearchResultElement> members) {
            mapper.batchWrite(members, new ArrayList<>());
        }

        public void writeCallResult(final ToolCallResult result) {
            //mapper.save(result);
        }

        public List<ToolCallRequest> getAllToolCallRequest() {
            // return mapper.scan(ToolCallRequest.class, new DynamoDBScanExpression());
            return null;
        }


        public CreateTableResult createTable(CreateTableRequest request) {
            return db.createTable(request);
        }

        public DeleteTableResult deleteTable(DeleteTableRequest request) {
            return db.deleteTable(request);
        }


    }
}
