package at.madlmayr;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.handlers.TracingHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DynamoAbstraction {

    private final AmazonDynamoDB dynamoClient;
    private static final String RAWDATA_TABLE_NAME = "RawData";
    private static final Logger LOGGER = LogManager.getLogger(DynamoAbstraction.class);

    public DynamoAbstraction() {
        dynamoClient = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder())).build();
    }

    public void writeRawData(final String key, final String rawData) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("CompanyTool", new AttributeValue().withS(key));
        item.put("Timestamp", new AttributeValue().withN("" + Instant.now().getEpochSecond()));
        item.put("Data", new AttributeValue().withS(rawData));
        dynamoClient.putItem(RAWDATA_TABLE_NAME, item);
        LOGGER.info("Data successfully stored in RawData Table");
    }

}
