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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Arrays;

/**
 * @author kkirmse
 */
@RestController
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserController {

    @Autowired
    private UserProperties userDetailsService;

    /**
     * <b>Copy@Paste from tut-spring-security-and-angular-js/oauth2-vanilla</b>
     * @param user current logged in user (from request/session)
     * @return current logged in user
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public Principal user(Principal user) {
        return user;
    }

    /* **********************************************************
    Additions to add more users or delete some
     *********************************************************** */

    @RequestMapping(value = "/user",method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity createUser(@RequestBody final UserProperties.User user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            if (user.getRoles() == null) {
                user.setRoles(Arrays.asList("USER"));
            }
            if (userDetailsService.getUsers().contains(user)) {
                userDetailsService.getUsers().set(userDetailsService.getUsers().indexOf(user), user);
                return new ResponseEntity(HttpStatus.OK);
            } else {
                userDetailsService.addUser(user);
            }
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/user",method = {RequestMethod.DELETE})
    public ResponseEntity deleteUser(@RequestBody final UserProperties.User user) {
        if (user.getName() != null && !user.getName().isEmpty()) {
            userDetailsService.addUser(user);
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
