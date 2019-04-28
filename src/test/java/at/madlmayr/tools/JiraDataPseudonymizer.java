package at.madlmayr.tools;

import at.madlmayr.jira.JiraSearchResultElement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiraDataPseudonymizer {

    private static final Logger LOGGER = LogManager.getLogger(JiraDataPseudonymizer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newLastNames;
    private List<String> newFirstNames;

    public JiraDataPseudonymizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(final String[] args) throws Exception {
        JiraDataPseudonymizer harmonizer = new JiraDataPseudonymizer("jira_01.raw", "jira_01.json");
        harmonizer.processData();
    }


    public void processData() throws Exception {
        String jiraContentFromFile = FileUtils.readFromFile("/" + inputFile);
        newLastNames = FileUtils.readNames("/last-names.txt");
        newFirstNames = FileUtils.readNames("/first-names.txt");

        // we call the shuffeling twice.
        FileUtils.writeToFile(replaceName(replaceName(jiraContentFromFile)), outputFile);
    }

    private String replaceName(final String inputJson) throws Exception {
        JiraSearchResultElement[] response = objectMapper.readValue(inputJson, JiraSearchResultElement[].class);

        Map<String, String> lastNameList = new HashMap<>();
        Map<String, String> firstNameList = new HashMap<>();

        for (JiraSearchResultElement member : response) {

            String replaceFirstName;
            String replaceLastName;
            if (member.getName().contains(".")) {

                String[] names = member.getName().split("\\.");
                if (names[0] != null && firstNameList.containsKey(names[0])) {
                    replaceFirstName = firstNameList.get(names[0]);
                } else {
                    replaceFirstName = newFirstNames.get(Math.abs(names[0].hashCode() % newFirstNames.size()));
                    firstNameList.put(names[0], replaceFirstName);
                }

                if (names[1] != null && lastNameList.containsKey(names[1])) {
                    replaceLastName = firstNameList.get(names[1]);
                } else {
                    replaceLastName = newLastNames.get(Math.abs(names[1].hashCode() % newLastNames.size()));
                    lastNameList.put(names[0], replaceLastName);
                }
            } else {
                replaceFirstName = newFirstNames.get(Math.abs(member.getName().hashCode() % newFirstNames.size()));
                replaceLastName = newLastNames.get(Math.abs(member.getName().hashCode() % newLastNames.size()));
            }


            URL url = new URL("https://jira.sim-technik.de/rest/api/2/user?username=" + (replaceFirstName + "." + replaceLastName).toLowerCase());
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            member.setSelf((uri.toASCIIString()));
            member.setKey((replaceFirstName + "." + replaceLastName).toLowerCase());
            member.setName((replaceFirstName + "." + replaceLastName).toLowerCase());
            member.setDisplayName(replaceFirstName + " " + replaceLastName);


            if (member.getEmail() != null && member.getEmail().contains("@")) {

                String email;
                if (member.getEmail().split("@").length == 1) {
                    LOGGER.info("Email {}", member.getEmail());
                    email = (replaceFirstName + "." + replaceLastName).toLowerCase() + "@demo.info";
                } else {
                    email = (replaceFirstName + "." + replaceLastName).toLowerCase() + "@" + member.getEmail().split("@")[1];
                }
                member.setEmail(email);
            } else {
                LOGGER.warn("No Email given: {}", member.getEmail());
            }

        }
        return objectMapper.writeValueAsString(response);
    }

}
