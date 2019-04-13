package at.madlmayr.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URL;
import java.util.List;

public class ArtifactoryDataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(ArtifactoryDataHarmonizer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newLastNames;
    private List<String> newFirstNames;

    public ArtifactoryDataHarmonizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
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
        ArtifactoryListElementWithUser[] response = objectMapper.readValue(inputJson, ArtifactoryListElementWithUser[].class);

        for (ArtifactoryListElementWithUser member : response) {
            LOGGER.info(member.getUser().getName()); // so this the UPN

            String replaceLastName = newLastNames.get(Math.abs(member.getUser().getName().hashCode() % newLastNames.size()));
            String replaceFirstName = newFirstNames.get(Math.abs(member.getUser().getName().hashCode() % newFirstNames.size()));


            String newName = FileUtils.generateRandomName(replaceFirstName, replaceLastName);

            member.getUser().setName(newName);
            member.getUser().setEmail(replaceFirstName + "." + replaceLastName + "@example.com");
            member.getListElement().setName(newName);

            URL url = new URL("http://localhost/gma/api/security/users/" + newName);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            member.getListElement().setUri(uri.toASCIIString());
        }
        return objectMapper.writeValueAsString(response);
    }


}
