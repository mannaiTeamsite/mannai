import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestCreateTicketAPI {
    public static void main(String[] args) {
        URL url = null;
        try {
            url = new URL("http://localhost:8082/api/contact/center/ticket");

            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            String jsonInputString = "{\"idType\":\"QID\",\"qid\":27373325655,\"eid\":0,\"passport\":\"\",\"nationality\":\"IN\",\"fullName\":\"Rajeshkumar\",\"companyName\":\"ABC\",\"phoneNo\":\"87654321\",\"emailId\":\"rajesh@t.com\",\"eServices\":10002,\"services\":17,\"eServiceName\":\"Health Cards\",\"serviceName\":\"Issue and Renewal of Health Cards\",\"subject\":\"Transaction failed\",\"comments\":\"Testing\",\"comments2\":\"123\",\"verification\":\"?\",\"recaptchaChallengeField\":\"?\",\"recaptchaResponseField\":\"?\",\"user\":\"testgg\",\"fromService\":false,\"attachment\":{\"fileName\":\"test.txt\",\"fileSize\":\"2kb\",\"content\":\"dGVzdA==\",\"fileType\":\"text\",\"filePath\":\"?\",\"id\":0}}";

            System.out.println("Input --");
            System.out.println(jsonInputString);

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            System.out.println("Start---");
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response --- " + response.toString());
            }
        } catch (Exception e) {
            System.out.println("Error - "+e.getMessage());
        } finally {
            System.out.println("End---");
        }
    }
}
