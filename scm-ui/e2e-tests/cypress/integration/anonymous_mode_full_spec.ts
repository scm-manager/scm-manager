/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
