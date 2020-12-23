package com.hukoomi.livesite.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class QueryTicket extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /** logger.debug object to check the flow of the code. */
    private static final Logger LOGGER =
            Logger.getLogger(QueryTicket.class);

    /**
     * @see HttpServlet#doPost(HttpServletRequest request,
     *      HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException {
        LOGGER.info("Customer Service Query Ticket: Start");
        StringBuilder resp = null;
        try {

            BufferedReader inbr = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = inbr.readLine();
            JSONObject data = new JSONObject(json);
            String ticketNo = data.getString("ticketNumber");
            String httpServletAddress = request.getLocalAddr();
            URL url = null;
            url = new URL(
                    "http://"+httpServletAddress + ":8082/api/contact/center/ticket/" + ticketNo);
            HttpURLConnection con =
                    (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    con.getInputStream(), StandardCharsets.UTF_8));
            resp = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                resp.append(responseLine.trim());
            }

        } catch (Exception e) {
            LOGGER.error("Customer Service Query Ticket: Exception ", e);
        } finally {
            LOGGER.info("End of Query Ticket");
        }
        response.setContentType("text/html;charset=UTF-8");
        try {
            response.getWriter().write(resp.toString());
        } catch (IOException e) {
            LOGGER.error("Customer Service Query Ticket: Exception ", e);
        }
    }

}
