/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.systemtests.steps;

import com.codeborne.selenide.Selenide;
import io.cucumber.java.en.Given;
import org.opennms.horizon.systemtests.CucumberHooks;
import org.opennms.horizon.systemtests.pages.AlternativeLoginPage;
import org.opennms.horizon.systemtests.pages.LoginPage;
import org.opennms.horizon.systemtests.pages.LokahiWalkthrough;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;

public class SetupSteps {
    private static final Logger LOG = LoggerFactory.getLogger(SetupSteps.class);
    private static final Network network = Network.newNetwork();

    public static Network getCommonNetwork() {
        return network;
    }

    @Given("Logged in with a minion {string}")
    public static void loggedInWithANamedMinion(String minionName) {
        login();

        if (LokahiWalkthrough.containsWalkthroughButton()) {
            String walkthroughMinionName = "default-minion";
            if (LocationSteps.isLocationNameDefault()) {
                walkthroughMinionName = minionName;
            }
            // Need to add a default minion
            LokahiWalkthrough.startSetup();
            LokahiWalkthrough.addMinionUsingWalkthrough(walkthroughMinionName);
            // Ensure we're at the main page once the minion is connected
            Selenide.open(CucumberHooks.instanceUrl);

            // If the location name isn't the default, we'll still need to add another minion
            if (! LocationSteps.isLocationNameDefault()) {
                MinionSteps.addMinionForLocation(minionName);
            }
        } else {
            MinionSteps.addMinionForLocation(minionName);
        }
    }

    public static void login() {
        try {
            Selenide.open(CucumberHooks.instanceUrl);
        } catch (Exception e) {
            LOG.info("Exception opening url, continuing: " + e);
        }

        if (CucumberHooks.keycloakLogin) {
            LoginPage.login();
        } else {
            AlternativeLoginPage.login();
        }

        LokahiWalkthrough.waitOnWalkthroughOrMain();
    }

    @Given("Logged in at welcome wizard")
    public void loggedInAtWelcomeWizard() {
        login();

        // Force going to the welcome wizard just in case
        Selenide.open(CucumberHooks.instanceUrl + "/welcome");
    }
}
