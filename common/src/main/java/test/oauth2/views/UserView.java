/*
 * Copyright 2015 The original authors
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
package test.oauth2.views;

import test.oauth2.ui.Sections;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.spring.sidebar.annotation.FontAwesomeIcon;
import org.vaadin.spring.sidebar.annotation.SideBarItem;

/**
 * <b>Copy&Paste from vaadin4spring/samples/security-sample-shared</b>
 *
 * View that is available for all users.
 *
 * @author Petter Holmström (petter@vaadin.com)
 */
@Secured({"ROLE_USER", "ROLE_ADMIN"})
@SpringView(name = "user")
@SideBarItem(sectionId = Sections.VIEWS, caption = "User View")
@FontAwesomeIcon(FontAwesome.ARCHIVE)
public class UserView extends CustomComponent implements View {

    public UserView() {
        Button button = new Button("Call user backend");
        setCompositionRoot(button);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }
}
