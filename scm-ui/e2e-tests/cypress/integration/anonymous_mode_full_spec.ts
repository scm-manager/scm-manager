/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { hri } from "human-readable-ids";

describe("Anonymous Mode Full", () => {
  beforeEach(() => {
    cy.restSetAnonymousMode("FULL");
  });

  it("should show login button on anonymous route", () => {
    // Arrange
    cy.restSetUserPermissions("_anonymous", ["repository:read,pull:*"]);
    // Act
    cy.visit("/repos/");
    // Assert
    cy.byTestId("repository-overview-filter");
    cy.byTestId("primary-navigation-login");
  });

  it("should show login page", () => {
    // Act
    cy.visit("/login/");
    // Assert
    cy.byTestId("login-button");
  });

  it("should navigate to login page", () => {
    // Arrange
    cy.visit("/");
    // Act
    cy.byTestId("primary-navigation-login").click();
    // Assert
    cy.byTestId("login-button");
  });

  it("should redirect to repositories overview after login", () => {
    // Arrange
    const username = hri.random();
    const password = hri.random();
    cy.restCreateUser(username, password);

    // Act
    cy.login(username, password);

    // Assert
    cy.byTestId(username);
  });

  it("should not allow anonymous user to change password", () => {
    // Act
    cy.visit("/me/settings/");
    // Assert
    cy.containsNotByTestId("ul", "user-settings-link");
    cy.get("section").not("Change password");
  });
});
