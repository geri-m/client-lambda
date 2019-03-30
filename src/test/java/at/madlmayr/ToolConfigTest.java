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

public class ToolConfigTest {

    @Test
    public void parseToolConfigFromDynamoDbStructure() throws JsonProcessingException {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("Company", new AttributeValue("demo company"));
        dataFromDynamo.put("Bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("URL", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("Tool", new AttributeValue("slack"));

        ToolConfig config = new ToolConfig(dataFromDynamo);

        assertThat(config.getBearer().equals("some bearer"));
        assertThat(config.getUrl().equals("https://www.madlmayr.at"));
        assertThat(config.getTool().equals("slack"));
        assertThat(config.generateKey("slack").equals("demo company#slack"));

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(config);

        JSONObject configAsJson = new JSONObject(jsonString);
        assertThat(configAsJson.keySet().contains("Company"));
        assertThat(configAsJson.keySet().contains("Bearer"));
        assertThat(configAsJson.keySet().contains("URL"));
        assertThat(configAsJson.keySet().contains("Tool"));

        assertThat(configAsJson.getString("Company").equals("demo company"));
        assertThat(configAsJson.getString("Bearer").equals("some bearer"));
        assertThat(configAsJson.getString("URL").equals("https://www.madlmayr.at"));
        assertThat(configAsJson.getString("Tool").equals("slack"));

    }

    @Test
    public void missingCompany() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("Bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("URL", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("Tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolConfig(dataFromDynamo),
                        "Company not present in Record");

        assertTrue(thrown.getMessage().contains("Company not present in Record"));
    }

    @Test
    public void missingBearer() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("Company", new AttributeValue("demo company"));
        dataFromDynamo.put("URL", new AttributeValue("https://www.madlmayr.at"));
        dataFromDynamo.put("Tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolConfig(dataFromDynamo),
                        "Bearer not present in Record");

        assertTrue(thrown.getMessage().contains("Bearer not present in Record"));
    }

    @Test
    public void missingURL() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("Company", new AttributeValue("demo company"));
        dataFromDynamo.put("Bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("Tool", new AttributeValue("slack"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolConfig(dataFromDynamo),
                        "URL not present in Record");

        assertTrue(thrown.getMessage().contains("URL not present in Record"));
    }

    @Test
    public void missingTool() {
        Map<String, AttributeValue> dataFromDynamo = new HashMap<>();
        dataFromDynamo.put("Company", new AttributeValue("demo company"));
        dataFromDynamo.put("Bearer", new AttributeValue("some bearer"));
        dataFromDynamo.put("URL", new AttributeValue("https://www.madlmayr.at"));
        ToolCallException thrown =
                assertThrows(ToolCallException.class,
                        () -> new ToolConfig(dataFromDynamo),
                        "Tool not present in Record");

        assertTrue(thrown.getMessage().contains("Tool not present in Record"));
    }

}
