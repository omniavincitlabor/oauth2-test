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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.vaadin.spring.security.annotation.EnableVaadinSharedSecurity;


/**
 *
 * @author kkirmse
 */
@EnableOAuth2Sso
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)//"Lumpensammler"
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@EnableVaadinSharedSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private OAuth2RestTemplate restTemplate;

    @Value("${config.oauth2.logoutTokenUri}")
    private String logoutTokenUri;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/login/**").anonymous()
                .antMatchers("/logout").permitAll()
                .antMatchers("/vaadinServlet/UIDL/**").permitAll()
                .antMatchers("/vaadinServlet/HEARTBEAT/**").permitAll()
                .anyRequest().authenticated();

        http.csrf().disable();

        http.logout()
                // Single-Sign-Out - Logout-Handler
                // Forward logout information to auth. server
                // Clean local access-token - necessary because access token might be
                // still un-expired and would not be fetched from auth-server (see also OAuth2LogoutVaadinRequestStartListener)
                .addLogoutHandler((request, response, authentication) -> {
                    try {
                        //1. kill accesstoken on server
                        restTemplate.execute(logoutTokenUri, HttpMethod.GET, null, null);
                        logger.debug("Killed Token on AuthorizationServer");
                        //2. remove current stored accesstoken info and auth info
                        restTemplate.getOAuth2ClientContext().setAccessToken(null);
                        restTemplate.getOAuth2ClientContext().getAccessTokenRequest().setAuthorizationCode(null);
                        restTemplate.getOAuth2ClientContext().getAccessTokenRequest().setStateKey(null);
                        logger.debug("Set local Access Token to null.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID");


        http.sessionManagement().sessionAuthenticationStrategy(sessionAuthenticationStrategy());
    }


    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/VAADIN/**");
    }

    /**
     * The {@link SessionAuthenticationStrategy} must be available as a Spring bean for Vaadin4Spring.
     */
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new SessionFixationProtectionStrategy();
    }

}