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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

/**
 *
 *
 */
@ConfigurationProperties(prefix = "oauth-test")
public class UserProperties implements UserDetailsService {

    @Valid
    private List<User> users = new ArrayList<>();


    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        for (final UserProperties.User user : getAllConfiguredUsers()) {
            if (user.getName().equals(username)) {
                return new UserDetails(user);
            }
        }
        throw new UsernameNotFoundException(username + " could not be found as user!");
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }

    public void addUser(final User user){
        this.users.add(user);
    }

    public void removeUser(final User user){
        this.users.remove(user);
    }

    public List<UserProperties.User> getAllConfiguredUsers() {
        return users;
    }

    public static class User implements Serializable {

        private String name;
        private String password;
        private List<String> roles;
        private String email;

        public User() {
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String password) {
            this.password = password;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(final List<String> roles) {
            this.roles = roles;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return Objects.equals(name, user.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class UserDetails extends org.springframework.security.core.userdetails.User {
        private UserProperties.User user;

        public UserDetails(final UserProperties.User user) {
            super(user.getName(),
                    user.getPassword(),
                    new SimpleAuthorityMapper().mapAuthorities(
                            AuthorityUtils.createAuthorityList(user.getRoles()
                                    .toArray(new String[user.getRoles().size()]))));
            this.user = user;
        }

        public UserProperties.User getUser() {
            return user;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            final UserDetails that = (UserDetails) o;
            if (user == null && that.user == null) {
                return true;
            }
            if (user != null && that.user != null) {
                return Objects.equals(user.getName(), that.user.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), user.getName());
        }
    }

}