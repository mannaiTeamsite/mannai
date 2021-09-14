import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestGetTicketAPI {

    public static void main(String[] args) {
        URL url = null;
        try {
            String ticketNo = "SD8152691";

            url = new URL("http://localhost:8082/api/contact/center/ticket/"+ticketNo);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            con.setDoOutput(true);

            System.out.println("Start---");
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response --- "+response.toString());
            }
        } catch (Exception e) {
            System.out.println("Error - "+e.getMessage());
        } finally {
            System.out.println("End---");
        }
    }


}
