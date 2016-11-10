/*
 * Copyright 2016 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.oauth2;

import org.springframework.context.ApplicationContext;
import test.oauth2.sessionservice.SessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * <b>Single-Sign-Out</b>
 *
 * Killing AccessToken and stored Authentication of OAuth2-Client User
 * OAuth2-Client has to call this REST-Service to logout also on Server
 *
 *
 * @author kkirmse
 */
@RestController
public class OAuthLogoutController {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private ApplicationContext context;


    private CookieClearingLogoutHandler cookieLogoutHandler = new CookieClearingLogoutHandler("JSESSIONID");
    private SecurityContextLogoutHandler securityLogoutHandler = new SecurityContextLogoutHandler();

    public OAuthLogoutController() {
        securityLogoutHandler.setClearAuthentication(true);
        securityLogoutHandler.setInvalidateHttpSession(true);
    }


    @RequestMapping(value = "/oauth-logout", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            String tokenValue = authHeader.toLowerCase().replace("bearer", "").trim();
            //this is only for some bean-creation timing issue (do not autowire DefaultTokenServices it will fail)
            final Map<String, DefaultTokenServices> serivces = context.getBeansOfType(DefaultTokenServices.class);
            if (!serivces.isEmpty()) {
                final DefaultTokenServices service = serivces.values().iterator().next();
                service.revokeToken(tokenValue);
                logger.debug("revoked oauth2 token " + tokenValue);

                SecurityContext context = SecurityContextHolder.getContext();
                Authentication auth = context.getAuthentication();

                if (auth instanceof OAuth2Authentication) {
                    SessionListener.invalidateSessionOfAuthentication(((OAuth2Authentication) auth).getUserAuthentication());
                } else {
                    SessionListener.invalidateSessionOfAuthentication(auth);
                }

                securityLogoutHandler.logout(request, response, auth);
                cookieLogoutHandler.logout(request, response, auth);
            } else {
                logger.error("DefaultTokenServices not found");
            }
        }
    }

}
