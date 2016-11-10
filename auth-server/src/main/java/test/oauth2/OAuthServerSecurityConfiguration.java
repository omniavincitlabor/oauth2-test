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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.vaadin.spring.http.HttpService;
import org.vaadin.spring.security.annotation.EnableVaadinSharedSecurity;
import org.vaadin.spring.security.config.VaadinSharedSecurityConfiguration;
import org.vaadin.spring.security.shared.SavedRequestAwareVaadinAuthenticationSuccessHandler;
import org.vaadin.spring.security.shared.VaadinAuthenticationSuccessHandler;
import org.vaadin.spring.security.web.VaadinRedirectStrategy;

import java.util.Collection;
import java.util.Map;

/**
 * <b>Combination of Samples
 * <ul>
 *     <li>vaadin4spring/samples/security-sample-shared</li>
 *     <li>tut-spring-security-and-angular-js/oauth2-vanilla</li>
 *     <li>hatemjaber/oauth2-vanilla-custom</li>
 * </ul>
 * + some additions
 * </b>
 *
 * Security for login and oauth UI elements. Ordered between the auth server and
 * resource server.
 *
 * @author kkirmse
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
@EnableVaadinSharedSecurity
@Order(1)
public class OAuthServerSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserProperties userDetailsService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private OAuth2ClientProperties oAuth2ClientProperties;

    @Autowired
    private TokenStore tokenStore;

    @Bean
    @Primary
    public OAuth2RestTemplate oauth2RestTemplate(final OAuth2ClientContext oauth2ClientContext,
                                                 final OAuth2ProtectedResourceDetails details) {
        OAuth2RestTemplate template = new OAuth2RestTemplate(details,
                oauth2ClientContext);
        return template;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/",
                "/login**",
                "/oauth/**",
                "/logout",
                "/vaadinServlet/**");

        http.authorizeRequests()
                .antMatchers("/login**", "/css/**").permitAll()
                .antMatchers("/logout").permitAll()
                .antMatchers("/vaadinServlet/UIDL/**").permitAll()
                .antMatchers("/vaadinServlet/HEARTBEAT/**").permitAll()
                .antMatchers("/vaadinServlet/PUSH/**").permitAll()
                .anyRequest().authenticated();

            /*http.formLogin()
                    .loginPage("/login")
                    .permitAll();*/

        http.httpBasic().disable();
        http.csrf().disable();

        http.logout()
                // Single-Sign-Out - Logout-Handler
                // Handle Logout of Authentication Server directly
                // -> revoke all of the accessTokens of the logged out user
                //
                .addLogoutHandler((request, response, authentication) -> {
                    //Autowiring does not work - but getting TokenService from applicationContext works
                    Map<String, DefaultTokenServices> services = applicationContext.getBeansOfType(DefaultTokenServices.class);
                    if (!services.isEmpty()) {
                        DefaultTokenServices service = services.values().iterator().next();
                        Collection<OAuth2AccessToken> token = tokenStore
                                .findTokensByClientIdAndUserName(oAuth2ClientProperties.getClientId(), authentication.getName());
                        for (OAuth2AccessToken t : token) {
                            service.revokeToken(t.getValue());
                        }
                    }
                })
                .clearAuthentication(true)
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID");

        http.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));

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

    @Bean(name = VaadinSharedSecurityConfiguration.VAADIN_AUTHENTICATION_SUCCESS_HANDLER_BEAN)
    VaadinAuthenticationSuccessHandler vaadinAuthenticationSuccessHandler(HttpService httpService,
                                                                          VaadinRedirectStrategy vaadinRedirectStrategy) {
        return new SavedRequestAwareVaadinAuthenticationSuccessHandler(httpService, vaadinRedirectStrategy, "/");
    }

    @Bean
    public InMemoryTokenStore inMemoryTokenStoreBean() {
        return new InMemoryTokenStore();
    }
}
