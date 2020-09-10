package com.hukoomi.livesite.controller;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.external.CookieHash;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("squid:S1874")
public class UserPersonaController {
    /** Logger object to check the flow of the code. */
    private static final Logger LOGGER = Logger.getLogger(
            UserPersonaController.class);
    /** This method will be called from Page Controller
     * to redirect the user based on his persona.
     * @param context The parameter context object passed from Controller.
     *
     * @return Redirect to the persona page or
     * open the requested page.
     */
    public final ForwardAction personaRedirect(final RequestContext context) {
        try {
            CookieHash cookies = context.getCookies();
            Cookie personaCookie = cookies.getCookie("persona");

            if (personaCookie != null) {
                LOGGER.debug("[UserPersonaController].[personaRedirect] "
                        + ":: personaCookie found ");
                String personaCookieValue = personaCookie.getValue();
                LOGGER.debug("[UserPersonaController].[personaRedirect] "
                        + ":: personaCookieValue : " + personaCookieValue);
                if (personaCookieValue != null && !personaCookieValue
                        .equalsIgnoreCase("")) {
                    LOGGER.debug("[UserPersonaController].[personaRedirect] "
                            + ":: sitePath : " + context.getSite().getPath());
                    String pagePath = context.getSite().getPath()
                            .concat("/").concat(personaCookieValue)
                            .concat(".page");

                    LOGGER.debug("[UserPersonaController].[personaRedirect] "
                            + ":: pagePath : " + pagePath);


                    if (context.getFileDAL().isFile(pagePath)) {
                        context.getResponse()
                                .setStatus(HttpServletResponse
                                        .SC_MOVED_PERMANENTLY);
                        context.getResponse().sendRedirect(personaCookieValue
                                .concat(".page"));
                    } else {
                        LOGGER.debug("[UserPersonaController]"
                                + ".[personaRedirect]"
                                + ":: pagePath : " + pagePath
                                + " doesnot exist. ");
                    }
                }
            } else {
                LOGGER.debug("[UserPersonaController].[personaRedirect] "
                        + ":: personaCookie not found ");
            }
        } catch (IOException e) {
            LOGGER.error("Exception occured :: " + e.getLocalizedMessage());
          //  e.printStackTrace();
        } catch (NullPointerException e) {
            LOGGER.error("Exception occured :: " + e.getLocalizedMessage());
           // e.printStackTrace();
        }  catch (Exception e) {
            LOGGER.error("Exception occured :: " + e.getLocalizedMessage());
            //e.printStackTrace();
        }
        LOGGER.debug("[UserPersonaController].[personaRedirect] "
                + " :: Exit");
        return null;
    }
}
