package at.madlmayr.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ArtifactoryDataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(ArtifactoryDataHarmonizer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newLastNames;
    private List<String> newFirstNames;

    public ArtifactoryDataHarmonizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(final String[] args) throws Exception {
        ArtifactoryDataHarmonizer harmonizer = new ArtifactoryDataHarmonizer("artifactory_01.raw", "artifactory_01.json");
        harmonizer.processData();
    }


    public void processData() throws Exception {
        String artifactoryContentFromFile = FileUtils.readFromFile("/" + inputFile);
        newLastNames = FileUtils.readNames("/last-names.txt");
        newFirstNames = FileUtils.readNames("/first-names.txt");

        // we call the shuffeling twice.
        FileUtils.writeToFile(replaceName(replaceName(artifactoryContentFromFile)), outputFile);
    }

    private String replaceName(final String inputJson) throws Exception {
        ArtifactoryListElementWithUser[] response = mapper.readValue(inputJson, ArtifactoryListElementWithUser[].class);

        for (ArtifactoryListElementWithUser member : response) {
            LOGGER.info(member.getUser().getName()); // so this the UPN

            String replaceLastName = newLastNames.get(Math.abs(member.getUser().getName().hashCode() % newLastNames.size()));
            String replaceFirstName = newFirstNames.get(Math.abs(member.getUser().getName().hashCode() % newFirstNames.size()));


            String newName = FileUtils.generateRandomName(replaceFirstName, replaceLastName);

            member.getUser().setName(newName);
            member.getUser().setEmail(replaceFirstName + "." + replaceLastName + "@example.com");
            member.getListElement().setUri("http://localhost/gma/api/security/users/" + newName);
            member.getListElement().setName(newName);
        }
        return mapper.writeValueAsString(response);
    }


}
