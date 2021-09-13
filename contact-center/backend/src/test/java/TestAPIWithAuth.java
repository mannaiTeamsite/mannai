import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class TestAPIWithAuth {
    public static void main(String[] args) {
        URL url = null;
        try {
            url = new URL("http://localhost:8088/mockContactCenter");

            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestMethod("POST");
            String encoded = Base64.getEncoder().encodeToString(("Hukoomi:webPortal@EMS").getBytes(StandardCharsets.UTF_8));  //Java 8
            con.setRequestProperty("Authorization", "Basic "+encoded);

            con.setDoOutput(true);
            String jsonInputString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soapenv:Envelope xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "    <soapenv:Header>\n" +
                    "        <InternationalizationContext xmlns=\"http://www.ibm.com/webservices/InternationalizationContext\" soapenv:mustunderstand=\"0\">\n" +
                    "            <Locales xmlns=\"\">\n" +
                    "                <Locale>\n" +
                    "                    <LanguageCode>en</LanguageCode>\n" +
                    "                </Locale>\n" +
                    "                <Locale>\n" +
                    "                    <LanguageCode>ltr</LanguageCode>\n" +
                    "                </Locale>\n" +
                    "            </Locales>\n" +
                    "            <TimeZoneId xmlns=\"\">GMT</TimeZoneId>\n" +
                    "        </InternationalizationContext>\n" +
                    "    </soapenv:Header>\n" +
                    "    <soapenv:Body>\n" +
                    "        <p880:RetrieveContactCenterRequest xmlns:p880=\"http://schemas.hp.com/SM/7\">\n" +
                    "            <p880:model>\n" +
                    "                <p880:keys xsi:nil=\"true\"/>\n" +
                    "                <p880:instance>\n" +
                    "                    <p880:CallID>S08152691</p880:CallID>\n" +
                    "                </p880:instance>\n" +
                    "            </p880:model>\n" +
                    "        </p880:RetrieveContactCenterRequest>\n" +
                    "    </soapenv:Body>\n" +
                    "</soapenv:Envelope>";
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            System.out.println("Start---");
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response --- "+response.toString());
            }
            System.out.println("End---");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
