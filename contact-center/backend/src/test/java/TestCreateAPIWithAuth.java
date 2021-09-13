import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestCreateAPIWithAuth {
    public static void main(String[] args) {
        URL url = null;
        try {
            url = new URL("https://motcsm.mirqab.gov.qa:13080/SM/7/ws");

            HttpURLConnection con = (HttpURLConnection)url.openConnection();

            con.setRequestMethod("POST");
            String encoded = Base64.getEncoder().encodeToString(("Hukoomi:webPortal@EMS").getBytes(StandardCharsets.UTF_8));  //Java 8
            con.setRequestProperty("Authorization", "Basic "+encoded);

            con.setDoOutput(true);
            String jsonInputString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<soapenv:Envelope\n" +
                    "    xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\"\n" +
                    "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                    "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "    <soapenv:Header>\n" +
                    "        <InternationalizationContext\n" +
                    "            xmlns=\"http://www.ibm.com/webservices/InternationalizationContext\" soapenv:mustUnderstand=\"0\">\n" +
                    "            <Locales\n" +
                    "                xmlns=\"\">\n" +
                    "                <Locale>\n" +
                    "                    <LanguageCode>en</LanguageCode>\n" +
                    "                </Locale>\n" +
                    "                <Locale>\n" +
                    "                    <LanguageCode>ltr</LanguageCode>\n" +
                    "                </Locale>\n" +
                    "            </Locales>\n" +
                    "            <TimeZoneId\n" +
                    "                xmlns=\"\">GMT\n" +
                    "            \n" +
                    "            </TimeZoneId>\n" +
                    "        </InternationalizationContext>\n" +
                    "    </soapenv:Header>\n" +
                    "    <soapenv:Body>\n" +
                    "        <p880:CreateContactCenterRequest\n" +
                    "            xmlns:p880=\"http://schemas.hp.com/SM/7\" attachmentData=\"1\" attachmentInfo=\"1\" ignoreEmptyElements=\"0\">\n" +
                    "            <p880:model>\n" +
                    "                <p880:keys xsi:nil=\"true\"/>\n" +
                    "                <p880:instance>\n" +
                    "                    <p880:Description>\n" +
                    "                        <p880:Description>eService=Health Cards</p880:Description>\n" +
                    "                        <p880:Description>Service=Issue and Renewal of Health Cards</p880:Description>\n" +
                    "                        <p880:Description>Nationality=</p880:Description>\n" +
                    "                        <p880:Description>Application number (if available): 231313&#13; &#13;\n" +
                    "\n" +
                    "Card Holder QID (if available):&#13;\n" +
                    "\n" +
                    "Card Holder Name (if available):&#13;\n" +
                    "\n" +
                    "Transaction Date (if available):</p880:Description>\n" +
                    "                    </p880:Description>\n" +
                    "                    <p880:Category>incident</p880:Category>\n" +
                    "                    <p880:ContactEmail>test@t.com</p880:ContactEmail>\n" +
                    "                    <p880:ContactFullName>Tester</p880:ContactFullName>\n" +
                    "                    <p880:Title>Transaction failed </p880:Title>\n" +
                    "                    <p880:ServiceCategory>HUKOOMI</p880:ServiceCategory>\n" +
                    "                    <p880:CallOrigin>WebPortal</p880:CallOrigin>\n" +
                    "                    <p880:External>1</p880:External>\n" +
                    "                    <p880:ContactIdType>QID</p880:ContactIdType>\n" +
                    "                    <p880:ContactQID>28938800015</p880:ContactQID>\n" +
                    "                    <p880:ContactEID>0</p880:ContactEID>\n" +
                    "                    <p880:ContactPassportNumber/>\n" +
                    "                    <p880:ContactMobilePhone>50294287</p880:ContactMobilePhone>\n" +
                    "                    <p880:ContactCompanyName>Al khor International school</p880:ContactCompanyName>\n" +
                    "                </p880:instance>\n" +
                    "            </p880:model>\n" +
                    "        </p880:CreateContactCenterRequest>\n" +
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
