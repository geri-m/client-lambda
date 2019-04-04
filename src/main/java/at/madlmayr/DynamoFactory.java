package at.madlmayr;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DynamoFactory {

    public DynamoAbstraction create() {
        return new DynamoAbstraction();
    }

    public DynamoAbstraction create(final URL serviceEndpoint) {
        return new DynamoAbstraction(serviceEndpoint);
    }

    public static class DynamoAbstraction {

        private static final String RAWDATA_TABLE_NAME = "RawData";
        private static final Logger LOGGER = LogManager.getLogger(DynamoFactory.class);
        private final AmazonDynamoDB dynamoClient;


        private DynamoAbstraction() {
            dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        }

        public DynamoAbstraction(final URL serviceEndpoint) {
            dynamoClient = AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint.toString(), Regions.EU_CENTRAL_1.getName())).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
        }


        public void writeRawData(final String key, final String rawData, final long timestamp) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("CompanyTool", new AttributeValue().withS(key));
            item.put("Timestamp", new AttributeValue().withN("" + timestamp));
            item.put("Data", new AttributeValue().withS(rawData));
            dynamoClient.putItem(RAWDATA_TABLE_NAME, item);
            LOGGER.info("Data successfully stored in RawData Table");
        }
    }
}
