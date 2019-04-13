package at.madlmayr.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class JiraDataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(JiraDataHarmonizer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newLastNames;
    private List<String> newFirstNames;

    public JiraDataHarmonizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(final String[] args) throws Exception {
        SlackDataHarmonizer harmonizer = new SlackDataHarmonizer("jira_01.raw", "jira_01.json");
        harmonizer.processData();
    }


    public void processData() throws Exception {
        String jiraContentFromFile = FileUtils.readFromFile("/" + inputFile);
        newLastNames = FileUtils.readNames("/last-names.txt");
        newFirstNames = FileUtils.readNames("/first-names.txt");
    }

}
