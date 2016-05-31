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

import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * I had problems with FixedAuthoritiesExtractor.
 * Result of the extraction was some map entry content.
 * So I added some recursion and map awareness.
 *
 * @author kkirmse
 */
@Component
public class MapAwareAuthoritiesExtractor implements AuthoritiesExtractor {

    private static final String AUTHORITIES = "authorities";

    private final String delim = ",";

    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        String authorities = "ROLE_USER";
        if (map.containsKey(AUTHORITIES)) {
            Object temp = map.get(AUTHORITIES);
            authorities = commaSeparatedString(temp);
        }
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);
    }

    private String commaSeparatedString(Object temp) {
        if (temp instanceof Collection) {
            Collection<?> coll = (Collection<?>)temp;
            if (CollectionUtils.isEmpty(coll)) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            Iterator<?> it = coll.iterator();
            while (it.hasNext()) {
                sb.append(commaSeparatedString(it.next()));
                if (it.hasNext()) {
                    sb.append(delim);
                }
            }
            return sb.toString();
        }
        if (ObjectUtils.isArray(temp)) {
            Object[] arr = (Object[])temp;
            if (ObjectUtils.isEmpty(arr)) {
                return "";
            }
            if (arr.length == 1) {
                return ObjectUtils.nullSafeToString(arr[0]);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) {
                    sb.append(delim);
                }
                sb.append(commaSeparatedString(arr[i]));
            }
            return sb.toString();
        }
        if (temp instanceof Map) {
            Map map = (Map)temp;
            return commaSeparatedString(map.values());
        }
        if (temp == null) {
            return "";
        }
        return temp.toString();
    }
}
