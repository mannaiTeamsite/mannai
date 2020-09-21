package com.hukoomi.livesite.controller;

import com.interwoven.livesite.common.web.CookieUtils;
import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;
import org.apache.log4j.Logger;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;



public class UserPersonaController {
    /** Constant for cookie name. */
    private static final String DEFAULT_COOKIE = "persona";

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
            LOGGER.debug("[UserPersonaController].[personaRedirect] "
                    + " :: Enter");

            String cookieName = context.getParameterString(
                    "cookieName", DEFAULT_COOKIE);
                 if (cookieName != null && !cookieName
                         .equalsIgnoreCase("")) {
                     LOGGER.debug("[UserPersonaController].[personaRedirect] "
                             + " :: cookieName : " + cookieName);
                     String personaCookieValue = CookieUtils
                             .getValue(context
                                     .getRequest(), cookieName);

                     if (personaCookieValue != null && !personaCookieValue
                             .equalsIgnoreCase("")) {
                         LOGGER.debug("[UserPersonaController]"
                                 + ".[personaRedirect] "
                                 + ":: sitePath : "
                                 + context.getSite().getPath());
                         String pagePath = context.getSite().getPath()
                                 .concat("/").concat(personaCookieValue)
                                 .concat(".page");

                         LOGGER.debug("[UserPersonaController]"
                                 + ".[personaRedirect] "
                                 + ":: pagePath : " + pagePath);


                         if (context.getFileDAL().isFile(pagePath)) {
                             context.getResponse()
                                     .setStatus(HttpServletResponse
                                             .SC_MOVED_PERMANENTLY);
                             context.getResponse()
                                     .sendRedirect(personaCookieValue
                                             .concat(".page"));
                         } else {
                             LOGGER.debug("[UserPersonaController]"
                                     + ".[personaRedirect]"
                                     + ":: pagePath : " + pagePath
                                     + " doesnot exist. ");
                         }
                     } else {
                         LOGGER.debug("[UserPersonaController]"
                                 + ".[personaRedirect]"
                                 + ":: cookie does not exist "
                                 + " or content value is Null ");
                     }
                 } else {
                     LOGGER.debug("[UserPersonaController]"
                             + ".[personaRedirect]"
                             + ":: Cookie name is not set "
                             + "from controller parameters ");
                 }
        } catch (IOException e) {
            LOGGER.error("Exception occured :: " + e.getMessage());
            LOGGER.error("Exception :: " + e.toString());
        } catch (NullPointerException e) {
            LOGGER.error("Exception occured :: " + e.getMessage());
            LOGGER.error("Exception :: " + e.toString());
        }  catch (Exception e) {
            LOGGER.error("Exception occured :: " + e.getMessage());
            LOGGER.error("Exception :: " + e.toString());
        }
        LOGGER.debug("[UserPersonaController].[personaRedirect] "
                + " :: Exit");
        return null;
    }
}
