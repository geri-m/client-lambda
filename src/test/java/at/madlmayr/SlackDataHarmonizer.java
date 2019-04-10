package at.madlmayr;

import at.madlmayr.slack.SlackMember;
import at.madlmayr.slack.SlackResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlackDataHarmonizer {

    private static final Logger LOGGER = LogManager.getLogger(SlackDataHarmonizer.class);


    public static void main(final String[] args) throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        StringBuffer buffer = new StringBuffer();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream("/slackdata_01.raw")))) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        }

        SlackResponse response = objectMapper.readValue(buffer.toString(), SlackResponse.class);
        String generatedTeamId = "T" + getRandomHexString(8).toUpperCase();

        for (SlackMember member : response.getMembers()) {
            member.setTeamId(generatedTeamId);
            member.getProfile().setTeam(generatedTeamId);
            member.setId("U" + getRandomHexString(8).toUpperCase());
            member.getProfile().setAvatarHash(getRandomHexString(12).toLowerCase());
            member.getProfile().setImage24(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-24.png"));
            member.getProfile().setImage32(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-32.png"));
            member.getProfile().setImage48(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-48.png"));
            member.getProfile().setImage72(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-72.png"));
            member.getProfile().setImage192(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-192.png"));
            member.getProfile().setImage512(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-512.png"));
            member.getProfile().setImage1024(new URL("https://i1.wp.com/a.slack-edge.com/00b63/img/avatars/ava_0007-1024.png"));
        }
    }

    // taken from: https://stackoverflow.com/questions/14622622/generating-a-random-hex-string-of-length-50-in-java-me-j2me
    private static String getRandomHexString(int numchars) {
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while (sb.length() < numchars) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, numchars);
    }

    private List<String> readNames() throws IOException {
        List<String> names = new ArrayList();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(CallUtils.class.getResourceAsStream("/slackdata_01.raw")))) {
            String line;
            while ((line = br.readLine()) != null) {
                names.add(line);
            }
        }
        return  names;
    }

}
