package com.hukoomi.livesite.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@MultipartConfig
public class SubmitTicket extends HttpServlet {

    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(SubmitTicket.class);

    /**
     * @see HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Customer Service Submit Ticket: Start");
        StringBuilder resp = null;
        boolean verify = true;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = br.readLine();
            String httpServletAddress = request.getLocalAddr();
            if (verify) {
                URL url = null;
                url = new URL("http://" + httpServletAddress + ":"
                        + "8082/api/contact/center/ticket");
                LOGGER.debug(" url::" + url);
                HttpURLConnection con =
                        (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                OutputStream os = con.getOutputStream();
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);

                BufferedReader respbr = new BufferedReader(
                        new InputStreamReader(con.getInputStream(),
                                StandardCharsets.UTF_8));
                resp = new StringBuilder();
                String responseLine = null;
                while ((responseLine = respbr.readLine()) != null) {
                    resp.append(responseLine.trim());
                }
            }

        } catch (IOException e) {
            response.setContentType("text/html;charset=UTF-8");
            try {
                response.getWriter().write("No data to create ticket");
            } catch (IOException e1) {
                LOGGER.error(" Exception Submit Service call ", e);
            }

        } catch (Exception e) {
            LOGGER.error(" Exception Submit Service call ", e);
        } finally {
            LOGGER.info("End of Submit Ticket");
        }
        response.setContentType("text/html;charset=UTF-8");
        try {
            if (verify) {
                if (resp != null) {
                    response.getWriter().write(resp.toString());
                }
            } else {
                response.getWriter().write("Recapcha Error! ");
            }
        } catch (IOException e) {
            LOGGER.error("Exception  Submit Service call", e);
        }

    }

}
