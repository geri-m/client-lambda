package at.madlmayr.tools;

import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackDataPseudonymizer {

    private static final Logger LOGGER = LogManager.getLogger(SlackDataPseudonymizer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newLastNames;
    private List<String> newFirstNames;

    public SlackDataPseudonymizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(final String[] args) throws Exception {
        SlackDataPseudonymizer harmonizer = new SlackDataPseudonymizer("slack_01.raw", "slack_01.json");
        harmonizer.processData();
    }


    public void processData() throws Exception {
        String slackContentFromFile = FileUtils.readFromFile("/" + inputFile);
        newLastNames = FileUtils.readNames("/last-names.txt");
        newFirstNames = FileUtils.readNames("/first-names.txt");

        // we call the shuffeling twice.
        FileUtils.writeToFile(replaceName(replaceName(slackContentFromFile)), outputFile);
    }

    private String replaceName(final String inputJson) throws Exception {
        SlackResponse response = objectMapper.readValue(inputJson, SlackResponse.class);

        Map<String, String> lastNameList = new HashMap<>();
        Map<String, String> firstNameList = new HashMap<>();

        for (SlackMember member : response.getMembers()) {
            // new Team ID is derived from the old id.
            String generatedTeamId = "T" + Integer.toHexString(member.getTeamId().hashCode()).toUpperCase();
            member.setTeamId(generatedTeamId);
            member.getProfile().setTeam(generatedTeamId);

            // Set userid with derived User ID from the initial one.
            member.setId("U" + Integer.toHexString(member.getId().hashCode()).toUpperCase());

            // replace avatar images
            member.getProfile().setPhone("");
            member.getProfile().setImageOriginal(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007.png"));
            member.getProfile().setImage24(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-24.png"));
            member.getProfile().setImage32(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-32.png"));
            member.getProfile().setImage48(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-48.png"));
            member.getProfile().setImage72(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-72.png"));
            member.getProfile().setImage192(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-192.png"));
            member.getProfile().setImage512(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-512.png"));
            member.getProfile().setImage1024(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-1024.png"));

            // for Bots we don't change the name.
            if (member.isBot()) {
                continue;
            }

            // Generate new Name out of Name List
            String replaceLastName = createNewLastName(member, lastNameList);

            // Generate new Firstname out of Firstname List
            String replaceFirstName = createNewFristName(member, firstNameList);

            // Replace the name in the member object
            replaceNames(member, replaceLastName, replaceFirstName);
        }
        return objectMapper.writeValueAsString(response);
    }

    private String createNewLastName(final SlackMember member, Map<String, String> lastNameList) {
        // Replace Lastname with Random Lastname
        String replaceLastName;

        String inputLastName = member.getProfile().getLastName();

        if (inputLastName == null) {
            if ((member.getProfile().getRealName().contains(" ") || member.getProfile().getRealName().contains("."))) {
                inputLastName = member.getProfile().getRealName().split("\\.| ")[0];
            } else {
                inputLastName = "noName";
                LOGGER.warn("Unable to extract a Name out of '{}'", member.getProfile().getRealName());
            }
        }

        if (!lastNameList.containsKey(inputLastName)) {
            replaceLastName = newLastNames.get(Math.abs(inputLastName.hashCode() % newLastNames.size()));
            lastNameList.put(inputLastName, replaceLastName);
        } else {
            replaceLastName = lastNameList.get(inputLastName);
        }

        return replaceLastName;
    }

    private String createNewFristName(final SlackMember member, Map<String, String> firstNameList) {
        String replaceFirstName;

        String inputFirstName = member.getProfile().getFirstName();

        if (inputFirstName == null) {
            if ((member.getProfile().getRealName().contains(" ") || member.getProfile().getRealName().contains("."))) {
                inputFirstName = member.getProfile().getRealName().split("\\.| ")[1];
            } else {
                inputFirstName = "noFirstame";
                LOGGER.warn("Unable to extract a First Name out of '{}'", member.getProfile().getRealName());
            }
        }

        if (!firstNameList.containsKey(inputFirstName)) {
            replaceFirstName = newFirstNames.get(Math.abs(inputFirstName.hashCode() % newFirstNames.size()));
            firstNameList.put(inputFirstName, replaceFirstName);
        } else {
            replaceFirstName = firstNameList.get(inputFirstName);
        }

        return replaceFirstName;
    }

    private void replaceNames(final SlackMember member, final String replaceLastName, final String replaceFirstName) {

        member.setName(FileUtils.generateRandomName(replaceFirstName, replaceLastName)); // UPN bei SSO

        member.setRealName(replaceFirstName + " " + replaceLastName);
        member.getProfile().setRealName(replaceFirstName + " " + replaceLastName);
        member.getProfile().setRealNameNormalized(replaceFirstName + " " + replaceLastName);

        member.getProfile().setDisplayName(replaceFirstName + " " + replaceLastName);
        member.getProfile().setDisplayNameNormalized(replaceFirstName + " " + replaceLastName);

        member.getProfile().setFirstName(replaceFirstName);
        member.getProfile().setLastName(replaceLastName);

        // fake the Email address with <newfirstname>.<newlastname>@<old-host>
        if (member.getProfile().getEmail() != null) {
            member.getProfile().setEmail(replaceFirstName + "." + replaceLastName + "@" + member.getProfile().getEmail().split("@")[1]);
        }
    }

}
