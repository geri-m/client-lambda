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
    }
}
