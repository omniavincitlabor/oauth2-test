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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.spring.sidebar.annotation.FontAwesomeIcon;
import org.vaadin.spring.sidebar.annotation.SideBarItem;

/**
 * When the user logs in and there is no view to navigate to, this view will be shown.
 *
 * <i>changed only the displayed text</i>
 *
 * @author Petter Holmström (petter@vaadin.com)
 * @auther kkirmse
 */
@SpringView(name = "")
@SideBarItem(sectionId = Sections.VIEWS, caption = "Home", order = 0)
@FontAwesomeIcon(FontAwesome.HOME)
public class HomeView extends VerticalLayout implements View {

    public HomeView() {
        setSpacing(true);
        setMargin(true);

        Label header = new Label("OAuth2 Single Sign On Demo Portal (2)!");
        header.addStyleName(ValoTheme.LABEL_H1);
        addComponent(header);

        Label body = new Label("<p>This application demonstrates a OAuth2 Client Portal in Vaadin, which uses as Vaadin based OAuth2 Authentication Server.</p>" +
                "<p>Current available users <i>tester/tester</i> and <i>admin/admin</i></p>" +
                "<p>Additional users can be added via REST-Service of Authentication Server</p>");
        body.setContentMode(ContentMode.HTML);
        addComponent(body);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }
}
