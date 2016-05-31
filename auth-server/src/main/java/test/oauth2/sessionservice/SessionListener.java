/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.oauth2.sessionservice;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * <b>99% Copy&Paste from JavaMelody</b>
 *
 *
 * @author kkirmse
 */
public class SessionListener implements HttpSessionListener {

    private static final String SESSION_ACTIVATION_KEY = "my.sessionActivation";
    private static final AtomicInteger SESSION_COUNT = new AtomicInteger();

    private boolean instanceEnabled;

    private static boolean instanceCreated;


    private static final ConcurrentMap<String, HttpSession> SESSION_MAP_BY_ID = new ConcurrentHashMap<String, HttpSession>();

    public SessionListener() {
        super();
        if (instanceCreated) {
            instanceEnabled = false;
        } else {
            instanceEnabled = true;
            setInstanceCreated(true);
        }
    }

    public static List<SessionInformations> getAllSessionsInformations() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession myCurrentSession = request.getSession(false);

        final Collection<HttpSession> sessions = SESSION_MAP_BY_ID.values();
        final List<SessionInformations> sessionsInformations = new ArrayList<SessionInformations>(
                sessions.size());
        for (final HttpSession session : sessions) {
            try {
                sessionsInformations.add(new SessionInformations(session, true, session.equals(myCurrentSession)));
            } catch (final Exception e) {
                // Tomcat can throw "java.lang.IllegalStateException: getLastAccessedTime: Session already invalidated"
                continue;
            }
        }
        sortSessions(sessionsInformations);
        return Collections.unmodifiableList(sessionsInformations);
    }


    public static void invalidateAllSessions() {
        invalidateAllSessionsExceptCurrentSession(null);
    }

    public static void invalidateAllSessionsExceptCurrentSession(HttpSession currentSession) {
        for (final HttpSession session : SESSION_MAP_BY_ID.values()) {
            try {
                if (currentSession != null && currentSession.getId().equals(session.getId())) {
                    continue;
                }
                session.invalidate();
            } catch (final Exception e) {
                continue;
            }
        }
    }

    public static void invalidateSession(String sessionId) {
        final HttpSession session = SESSION_MAP_BY_ID.get(sessionId);
        if (session != null) {
            try {
                session.invalidate();
            } catch (final Exception e) {
                return;
            }
        }
    }

    public static void invalidateSessionOfAuthentication(Authentication auth) {
        for (final HttpSession session : SESSION_MAP_BY_ID.values()) {
            try {
                Object attributeValue = session.getAttribute("SPRING_SECURITY_CONTEXT");
                if ( attributeValue != null &&
                        ((SecurityContext)attributeValue).getAuthentication().equals(auth)) {
                    session.removeAttribute("SPRING_SECURITY_CONTEXT");
                    invalidateSession(session.getId());
                }
            } catch (final Exception e) {
                continue;
            }
        }
    }

    private static List<SessionInformations> sortSessions(List<SessionInformations> sessionsInformations) {
        if (sessionsInformations.size() > 1) {
            Collections.sort(sessionsInformations,
                    Collections.reverseOrder((session1, session2) -> {
                        if (session1.getLastAccess().before(session2.getLastAccess())) {
                            return 1;
                        } else if (session1.getLastAccess().after(session2.getLastAccess())) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }));
        }
        return sessionsInformations;
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        if (!instanceEnabled) {
            return;
        }
        final HttpSession session = event.getSession();
        if (session.getAttribute(SESSION_ACTIVATION_KEY) == this) {
            for (final Map.Entry<String, HttpSession> entry : SESSION_MAP_BY_ID.entrySet()) {
                final String id = entry.getKey();
                final HttpSession other = entry.getValue();
                if (!id.equals(other.getId())) {
                    SESSION_MAP_BY_ID.remove(id);
                }
            }
        } else {
            session.setAttribute(SESSION_ACTIVATION_KEY, this);

            SESSION_COUNT.incrementAndGet();
        }

        SESSION_MAP_BY_ID.put(session.getId(), session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (!instanceEnabled) {
            return;
        }
        final HttpSession session = event.getSession();

        SESSION_COUNT.decrementAndGet();

        SESSION_MAP_BY_ID.remove(session.getId());
    }


    private static void setInstanceCreated(boolean newInstanceCreated) {
        instanceCreated = newInstanceCreated;
    }

    public static class SessionInformations implements Serializable {

        private static final long serialVersionUID = -2689338895804445093L;

        private final String id;
        private final Date lastAccess;
        private final Date age;
        private final Date expirationDate;
        private final int attributeCount;

        private final boolean isCurrentSession;

        @SuppressWarnings("all")
        private final List<SessionAttribute> attributes;

        public static class SessionAttribute implements Serializable {
            private static final long serialVersionUID = 4786854834871331127L;
            private final String name;
            private final String type;
            private final String content;

            public SessionAttribute(HttpSession session, String attributeName) {
                super();
                assert session != null;
                assert attributeName != null;
                name = attributeName;
                final Object value = session.getAttribute(attributeName);

                if (value == null) {
                    content = null;
                    type = null;
                } else {
                    String tmp;
                    try {
                        tmp = String.valueOf(value);
                    } catch (final Exception e) {
                        tmp = e.toString();
                    }
                    content = tmp;
                    type = value.getClass().getName();
                }
            }

            public String getName() {
                return name;
            }

            public String getType() {
                return type;
            }

            public String getContent() {
                return content;
            }


            @Override
            public String toString() {
                return "SessionAttribute{" +
                        "name='" + name + '\'' +
                        ", type='" + type + '\'' +
                        ", content='" + content + '\'' +
                        '}';
            }
        }

        public SessionInformations(HttpSession session, boolean includeAttributes, boolean isCurrentSession) {
            super();
            assert session != null;
            id = session.getId();
            final long now = System.currentTimeMillis();
            lastAccess = new Date(now - session.getLastAccessedTime());
            age = new Date(now - session.getCreationTime());
            expirationDate = new Date(session.getLastAccessedTime() + session.getMaxInactiveInterval()
                    * 1000L);

            final List<String> attributeNames = Collections.list(session.getAttributeNames());
            attributeCount = attributeNames.size();


            if (includeAttributes) {
                attributes = new ArrayList<>(attributeCount);
                for (final String attributeName : attributeNames) {
                    attributes.add(new SessionAttribute(session, attributeName));
                }
            } else {
                attributes = new ArrayList<>();
            }

            this.isCurrentSession = isCurrentSession;
        }


        public String getId() {
            return id;
        }

        public Date getLastAccess() {
            return lastAccess;
        }

        public Date getAge() {
            return age;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public int getAttributeCount() {
            return attributeCount;
        }

        public List<SessionAttribute> getAttributes() {
            return new ArrayList<>(attributes);
        }

        public boolean isCurrentSession() {
            return isCurrentSession;
        }

        @Override
        public String toString() {
            return "SessionInformations{" +
                    "id='" + id + '\'' +
                    ", lastAccess=" + lastAccess +
                    ", age=" + age +
                    ", expirationDate=" + expirationDate +
                    ", attributeCount=" + attributeCount +
                    ", attributes=" + attributes +
                    ", isCurrentSession=" + isCurrentSession +
                    '}';
        }
    }
}
