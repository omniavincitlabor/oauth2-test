OAuth2 Test
==============================
This is my testcase for an Spring-OAuth2 Authentication Server with 2 Spring based Portals that should use the Authentication Server for single sign on.
(I took some code snippets and ideas from
* https://github.com/spring-guides/tut-spring-security-and-angular-js/tree/master/oauth2-vanilla
* https://github.com/hatemjaber/oauth2-vanilla-custom )

The 2 portals as well as the Authentication Server use Vaadin for the user interface.
(I copied for this test case some stuff of the Vaadin-Spring-Sample https://github.com/peholmst/vaadin4spring/tree/master/samples/security-sample-shared)

After logout from either auth server or one of the two portals the user is logged out from all of the others as well. (my single-sign-out)
This single-sign-out is based on ideas of:
* http://stackoverflow.com/questions/21987589/spring-security-how-to-log-out-user-revoke-oauth2-token
* https://github.com/raonirenosto/silverauth/blob/master/src/main/java/com/silverauth/controller/TokenController.java

### Features

* OAuth Server with some own GUI (http://localhost:8081/uaa)
* 2 Portals (http://localhost:8083/uii and http://localhost:8085/uee)
* Single-Sign-On
* self-made Single-Sign-Out

### Use Cases

Here are my use cases, I tried to test with this testcase.

Credentials are
* tester/tester (uses USER role)
* admin/admin (uses ADMIn role)

#### 1. Login to one of the portals

1. user calls http://localhost:8085/uee
2. portal redirects to http://localhost:8081/uaa/login
3. user logs in
4. auth server back-redirects to http://localhost:8085/uee (you see "OAuth2 Single Sign On Demo Portal (2)!" heading)

#### 2. Login to auth server

(if executed after use case 1-3 you should close browser and open again)

1. user calls http://localhost:8081/uaa/login
2. user logs in
3. views of http://localhost:8081/uaa are shown (you see "Authorization Server Demo!" heading)

#### 3. Single-Sign-On

(if executed after use case 1-3 you should close browser and open again)

##### 3.a. login via portal

1. execute test case 1.
2. navigate to  http://localhost:8081/uaa (you should see views according to the authorities of the selected user)
3. navigate to  http://localhost:8083/uii (you should see views according to the authorities of the selected user)

##### 3.b. login directly on auth server

1. execute test case 2.
2. navigate to  http://localhost:8083/uii (you should see views according to the authorities of the selected user)
3. navigate to  http://localhost:8085/uee (you should see views according to the authorities of the selected user)

#### 4. Single-Sign-Out

1. execute test case 1
2. open new browser tab and open http://localhost:8083/uii
3. click "Logout" in either tab (you should be forwarded to login page)
4. go to other tab and change view (you should be forwarded to login page)