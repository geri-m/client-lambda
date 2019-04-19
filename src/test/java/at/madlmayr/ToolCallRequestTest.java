package at.madlmayr;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToolCallRequestTest {

    @Test
    public void parseToolConfigFromDynamoDbStructure() throws JsonProcessingException {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("company", new AttributeValue("demo company"));
        dataFromDynamo.put("bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("url", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("tool", new AttributeValue("slack"));

        ToolCallRequest config = new ToolCallRequest(dataFromDynamo, 1234, 1);

        assertThat(config.getBearer().equals("some bearer"));
        assertThat(config.getUrl().equals("https://www.madlmayr.at"));
        assertThat(config.getTool().equals("slack"));
        assertThat(config.generateKey("slack").equals("demo company#slack"));
        assertThat(config.getTimestamp() == (1234));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(config);

        JSONObject configAsJson = new JSONObject(jsonString);
        assertThat(configAsJson.keySet().contains("company"));
        assertThat(configAsJson.keySet().contains("bearer"));
        assertThat(configAsJson.keySet().contains("url"));
        assertThat(configAsJson.keySet().contains("tool"));
        assertThat(configAsJson.keySet().contains("timestamp"));

        assertThat(configAsJson.getString("company").equals("demo company"));
        assertThat(configAsJson.getString("bearer").equals("some bearer"));
        assertThat(configAsJson.getString("url").equals("https://www.madlmayr.at"));
        assertThat(configAsJson.getString("tool").equals("slack"));
        assertThat(configAsJson.getLong("timestamp") == 1234);

    }

    @Test
    public void missingCompany() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("url", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolCallRequest(dataFromDynamo, 1234, 1),
                        "Company not present in Record");

        assertTrue(thrown.getMessage().contains("Company not present in Record"));
    }

    @Test
    public void missingBearer() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("company", new AttributeValue("demo company"));
        dataFromDynamo.put("url", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolCallRequest(dataFromDynamo, 1234, 1),
                        "Bearer not present in Record");

        assertTrue(thrown.getMessage().contains("Bearer not present in Record"));
    }

    @Test
    public void missingURL() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("company", new AttributeValue("demo company"));
        dataFromDynamo.put("bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolCallRequest(dataFromDynamo, 1234, 1),
                        "URL not present in Record");

        assertTrue(thrown.getMessage().contains("URL not present in Record"));
    }

    @Test
    public void missingTool() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("company", new AttributeValue("demo company"));
        dataFromDynamo.put("bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("url", new AttributeValue("https://www.madlmayr.at"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolCallRequest(dataFromDynamo, 1234, 1),
                        "Tool not present in Record");

        assertTrue(thrown.getMessage().contains("Tool not present in Record"));
    }


}
