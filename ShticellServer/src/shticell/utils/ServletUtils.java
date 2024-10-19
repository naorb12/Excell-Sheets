package shticell.utils;

import engine.ShticellEngine;
import engine.users.UserManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.ServletContext;

import static shticell.constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String ENGINE_ATTRIBUTE_NAME = "engineManager";

    /*
        Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
        the actual fetch of them is remained un-synchronized for performance POV
         */
    private static final Object userManagerLock = new Object();
    private static final Object engineManagerLock = new Object();


    public static UserManager getUserManager(ServletContext servletContext) {

        UserManager userManager = (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
        if (userManager == null) {
            synchronized (userManagerLock) {
                if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                    servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
                }
            }
        }

        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static ShticellEngine getEngine(ServletContext servletContext) {
        ShticellEngine engine = (ShticellEngine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);

        if (engine == null) {
            synchronized (engineManagerLock) {
                engine = (ShticellEngine) servletContext.getAttribute(ENGINE_ATTRIBUTE_NAME);
                if (engine == null) {
                    engine = new ShticellEngine();
                    servletContext.setAttribute(ENGINE_ATTRIBUTE_NAME, engine);
                }
            }
        }

        return engine;
    }

    public static int getIntParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }

}

