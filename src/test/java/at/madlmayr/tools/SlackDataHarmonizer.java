package at.madlmayr.tools;

import at.madlmayr.CallUtils;
import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlackDataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(SlackDataHarmonizer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final String inputFile;
    private final String outputFile;
    private List<String> newNames;
    private List<String> newFirstName;

    public SlackDataHarmonizer(final String inputFile, final String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        // make the structure more beautyful
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static void main(final String[] args) throws Exception {
        SlackDataHarmonizer harmonizer = new SlackDataHarmonizer("slackdata_01.raw", "slackdata_01.json");
        harmonizer.processData();
    }

    private static String generateRandomName(final String firstname, final String name) {
        switch ((firstname + name).hashCode() % 3) {
            case 0:
                // mad0001g
                if (name.length() >= 3)
                    return (name.substring(0, 3) + ((firstname + name).hashCode() + "").substring(4, 8) + firstname.substring(0, 1)).toLowerCase();
                else
                    return firstname;
            case 1:
                return firstname;
            case 2:
            default:
                return firstname.substring(0, 1) + "." + name;
        }
    }

    private static List<String> readNames(final String name) throws IOException {
        List<String> names = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream(name)))) {
            String line;
            while ((line = br.readLine()) != null) {
                names.add(line);
            }
        }
        return names;
    }

    public void processData() throws Exception {
        String slackContentFromFile = FileUtils.readFromFile("/" + inputFile);
        newNames = readNames("/names.txt");
        newFirstName = readNames("/first-names.txt");

        // we call the shuffeling twice.
        FileUtils.writeToFile(replaceName(replaceName(slackContentFromFile)), outputFile);
    }

    private String replaceName(final String inputJson) throws Exception {
        SlackResponse response = mapper.readValue(inputJson, SlackResponse.class);

        Map<String, String> nameList = new HashMap<>();
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
            String replaceName = createNewName(member, nameList);

            // Generate new Firstname out of Firstname List
            String replaceFirstName = createNewFristName(member, firstNameList);

            // Replace the name in the member object
            replaceNames(member, replaceName, replaceFirstName);
        }
        return mapper.writeValueAsString(response);
    }

    private String createNewName(final SlackMember member, Map<String, String> nameList) {
        // Replace Lastname with Random Lastname
        String replaceName;

        String inputName = member.getProfile().getLastName();

        if (inputName == null) {
            if ((member.getProfile().getRealName().contains(" ") || member.getProfile().getRealName().contains("."))) {
                inputName = member.getProfile().getRealName().split("\\.| ")[0];
            } else {
                inputName = "noName";
                LOGGER.warn("Unable to extract a Name out of '{}'", member.getProfile().getRealName());
            }
        }

        if (!nameList.containsKey(inputName)) {
            replaceName = newNames.get(Math.abs(inputName.hashCode() % newNames.size()));
            nameList.put(inputName, replaceName);
        } else {
            replaceName = nameList.get(inputName);
        }

        return replaceName;
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
            replaceFirstName = newFirstName.get(Math.abs(inputFirstName.hashCode() % newFirstName.size()));
            firstNameList.put(inputFirstName, replaceFirstName);
        } else {
            replaceFirstName = firstNameList.get(inputFirstName);
        }

        return replaceFirstName;
    }

    private void replaceNames(final SlackMember member, final String replaceName, final String replaceFirstName) {

        member.setName(generateRandomName(replaceFirstName, replaceName)); // UPN bei SSO

        member.setRealName(replaceFirstName + " " + replaceName);
        member.getProfile().setRealName(replaceFirstName + " " + replaceName);
        member.getProfile().setRealNameNormalized(replaceFirstName + " " + replaceName);

        member.getProfile().setDisplayName(replaceFirstName + " " + replaceName);
        member.getProfile().setDisplayNameNormalized(replaceFirstName + " " + replaceName);

        member.getProfile().setFirstName(replaceFirstName);
        member.getProfile().setLastName(replaceName);

        // fake the Email address with <newfirstname>.<newlastname>@<old-host>
        if (member.getProfile().getEmail() != null) {
            member.getProfile().setEmail(replaceFirstName + "." + replaceName + "@" + member.getProfile().getEmail().split("@")[1]);
        }
    }

}
