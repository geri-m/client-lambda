package at.madlmayr;

import at.madlmayr.artifactory.ArtifactoryUser;
import at.madlmayr.jira.JiraSearchResultElement;
import at.madlmayr.slack.SlackMember;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoFactory {

    public DynamoAbstraction create() {
        return new DynamoAbstraction();
    }

    public DynamoAbstraction create(int port) {
        return new DynamoAbstraction(port);
    }

    public static class DynamoAbstraction {

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
            mapper.save(result);
        }

        public List<ToolCallConfig> getAllToolCallRequest() {
            return mapper.scan(ToolCallConfig.class, new DynamoDBScanExpression());
        }

        public ToolCallResult getLatestCallResultFor(final String company) {
            ToolCallResult query = new ToolCallResult();
            query.setCompany(company);
            query.setTimestamp(0L);
            DynamoDBQueryExpression<ToolCallResult> queryExpression = new DynamoDBQueryExpression<ToolCallResult>()
                    .withHashKeyValues(query);

            return null;
            // return mapper.query(ToolCallResult.class, queryExpression).get(0);
        }


        public List<ToolCallResult> getAllToolCallResult(final String company, final long batchTimeStamp) {
            DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                    .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                    .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
                    .build();

            // we create a dedicated mapper, as we need consistent reads.
            DynamoDBMapper mapper = new DynamoDBMapper(db, mapperConfig);
            ToolCallResult query = new ToolCallResult();
            query.setCompany(company);
            query.setTimestamp(batchTimeStamp);
            DynamoDBQueryExpression<ToolCallResult> queryExpression = new DynamoDBQueryExpression<ToolCallResult>()
                    .withHashKeyValues(query);

            return mapper.query(ToolCallResult.class, queryExpression);
        }

        public List<ToolCallResult> getAllToolCallResultUnfinished(final String company, final long batchTimeStamp) {
            DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
                    .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT)
                    .withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING)
                    .build();

            // we create a dedicated mapper, as we need consistent reads.
            DynamoDBMapper mapper = new DynamoDBMapper(db, mapperConfig);
            ToolCallResult query = new ToolCallResult();
            query.setCompany(company);
            query.setTimestamp(batchTimeStamp);
            // query.setAmountOfUsers(-1);

            Map<String, AttributeValue> eav = new HashMap<>();
            eav.put(":negativeUsers", new AttributeValue().withN("-1"));

            DynamoDBQueryExpression<ToolCallResult> queryExpression = new DynamoDBQueryExpression<ToolCallResult>()
                    .withHashKeyValues(query).withFilterExpression("amountOfUsers = :negativeUsers").withExpressionAttributeValues(eav).withConsistentRead(true);

            return mapper.query(ToolCallResult.class, queryExpression);
        }


        public CreateTableResult createTable(CreateTableRequest request) {
            return db.createTable(request);
        }

        public DeleteTableResult deleteTable(DeleteTableRequest request) {
            return db.deleteTable(request);
        }


    }
}
