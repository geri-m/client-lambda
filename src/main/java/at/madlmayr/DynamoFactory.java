package at.madlmayr;

import at.madlmayr.slack.SlackMember;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class DynamoFactory {

    public DynamoAbstraction create() {
        return new DynamoAbstraction();
    }

    public DynamoAbstraction create(final URL serviceEndpoint) {
        return new DynamoAbstraction(serviceEndpoint);
    }

    public static class DynamoAbstraction {

        private static final Logger LOGGER = LogManager.getLogger(DynamoFactory.class);
        private final AmazonDynamoDB dynamoClient;


        private DynamoAbstraction() {
            dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        }

        public DynamoAbstraction(final URL serviceEndpoint) {
            dynamoClient = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint.toString(), Regions.EU_CENTRAL_1.getName())).build();
        }

        public void writeSlackMember(final SlackMember member) {
            DynamoDBMapper mapper = new DynamoDBMapper(dynamoClient);
            mapper.save(member);
        }
    }
}
