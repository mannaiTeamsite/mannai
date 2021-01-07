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

import com.hukoomi.utils.ValidationUtils;
import com.hukoomi.utils.XssUtils;

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
            HttpServletResponse response) throws ServletException {
        LOGGER.info("Customer Service Query Ticket: Start");
        StringBuilder resp = null;
        JSONObject data = null;
        XssUtils xssUtils = new XssUtils();
        try {

            BufferedReader inbr = new BufferedReader(
                    new InputStreamReader(request.getInputStream()));
            String json = "";
            json = inbr.readLine();
            data = new JSONObject(json);
            String ticketNo = xssUtils.stripXSS(data.getString("ticketNumber"));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ValidationUtils util = new ValidationUtils();
            if (ticketNo.length() <= 20) {
                String httpServletAddress = request.getLocalAddr();
                URL url = null;
                url = new URL("http://" + httpServletAddress
                        + ":8082/api/contact/center/ticket/" + ticketNo);
                HttpURLConnection con =
                        (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(),
                                StandardCharsets.UTF_8));
                resp = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    resp.append(responseLine.trim());
                }
            } else {
                data.put("success", "false");
                data.put("errorMessage", "validationFailed");
                response.getWriter().write(data.toString());
            }

        } catch (IOException e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                data.put("success", "false");
                data.put("errorMessage", e.getMessage());
                response.getWriter().write(data.toString());
            } catch (IOException e1) {
                LOGGER.error("Customer Service Query Ticket: Exception ",
                        e);
            }

        } finally {
            LOGGER.info("End of Query Ticket");
        }

        try {
            response.getWriter().write(resp.toString());
        } catch (IOException e) {
            LOGGER.error("Customer Service Query Ticket: Exception ", e);
        }
    }

}
