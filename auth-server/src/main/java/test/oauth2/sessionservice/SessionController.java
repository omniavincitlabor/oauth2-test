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
package test.oauth2.sessionservice;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kkirmse
 */
@RestController
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionController {



    @RequestMapping(value = "/session",method = {RequestMethod.GET})
    public List<SessionListener.SessionInformations> sessionInformations() {

        return SessionListener.getAllSessionsInformations();
    }

    @RequestMapping(value = "/session",method = {RequestMethod.DELETE})
    public List<SessionListener.SessionInformations> deleteSession(@RequestParam(value = "sessionId", required = false) String sessionId) {

        if (sessionId != null) {
            SessionListener.invalidateSession(sessionId);
            return SessionListener.getAllSessionsInformations();
        } else {
            SessionListener.invalidateAllSessions();
            return SessionListener.getAllSessionsInformations();
        }
    }
}
