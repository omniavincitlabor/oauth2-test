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

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.stereotype.Component;
import org.vaadin.spring.request.VaadinRequestEndListener;

import javax.servlet.http.HttpSession;

/**
 * <b>Single-Sign-Out</b>
 *
 * Continuously check if Access-Token is still active on Auth-Server
 *
 * @author kkirmse
 */
@Component
public class OAuth2LogoutVaadinRequestStartListener implements VaadinRequestEndListener {

    @Autowired
    private OAuth2ProtectedResourceDetails resource;

    @Autowired
    private ResourceServerProperties sso;

    @Override
    public void onRequestEnd(VaadinRequest request, VaadinResponse response, VaadinSession vaadinSession) {
        if (request instanceof VaadinServletRequest) {
            HttpSession session = ((VaadinServletRequest) request).getHttpServletRequest().getSession(false);
            if (session != null && session.getAttribute("scopedTarget.oauth2ClientContext") != null) {

                DefaultOAuth2ClientContext current = (DefaultOAuth2ClientContext)session.getAttribute("scopedTarget.oauth2ClientContext");
                OAuth2AccessToken accessToken = current.getAccessToken();

                if (accessToken != null) {
                    //current.getAccessTokenRequest() nicht nutzbar

                    UserInfoTokenServices services = new UserInfoTokenServices(
                            this.sso.getUserInfoUri(), this.sso.getClientId());
                    services.setTokenType(this.sso.getTokenType());

                    try {
                        services.loadAuthentication(accessToken.getValue());
                    } catch (AuthenticationException | InvalidTokenException e) {
                        vaadinSession.close();
                        session.invalidate();
                    }
                }

            }
        }
    }


}
