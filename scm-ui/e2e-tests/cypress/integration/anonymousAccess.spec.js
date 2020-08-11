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

describe("With Anonymous mode disabled", () => {
  before("Disable anonymous access", () => {
    cy.login("scmadmin", "scmadmin");
    setAnonymousMode("OFF");
    cy.byTestId("primary-navigation-logout").click();
  });

  it("Should show login page without primary navigation", () => {
    cy.byTestId("login-button");
    cy.containsNotByTestId("div", "primary-navigation-login");
    cy.containsNotByTestId("div", "primary-navigation-repositories");
  });
  it("Should redirect after login", () => {
    cy.login("scmadmin", "scmadmin");

    cy.visit("/me");
    cy.byTestId("footer-user-profile");
    cy.byTestId("primary-navigation-logout").click();
  });
});

describe("With Anonymous mode protocol only enabled", () => {
  before("Set anonymous mode to protocol only", () => {
    cy.login("scmadmin", "scmadmin");
    setAnonymousMode("PROTOCOL_ONLY");
    cy.byTestId("primary-navigation-logout").click();
  });

  it("Should show login page without primary navigation", () => {
    cy.visit("/repos/");
    cy.byTestId("login-button");
    cy.containsNotByTestId("div", "primary-navigation-login");
    cy.containsNotByTestId("div", "primary-navigation-repositories");
  });

  after("Disable anonymous access", () => {
    cy.login("scmadmin", "scmadmin");
    setAnonymousMode("OFF");
    cy.byTestId("primary-navigation-logout").click();
  });
});

describe("With Anonymous mode fully enabled", () => {
  before("Set anonymous mode to full", () => {
    cy.login("scmadmin", "scmadmin");
    setAnonymousMode("FULL");

    // Give anonymous user permissions
    cy.byTestId("primary-navigation-users").click();
    cy.byTestId("_anonymous").click();
    cy.byTestId("user-settings-link").click();
    cy.byTestId("user-permissions-link").click();
    cy.byTestId("read-all-repositories").click();
    cy.byTestId("set-permissions-button").click();

    cy.byTestId("primary-navigation-logout").click();
  });

  it("Should show repositories overview with Login button in primary navigation", () => {
    cy.visit("/repos/");
    cy.byTestId("repository-overview-filter");
    cy.byTestId("SCM-Anonymous");
    cy.byTestId("primary-navigation-login");
  });
  it("Should show login page on url", () => {
    cy.visit("/login/");
    cy.byTestId("login-button");
  });
  it("Should show login page on link click", () => {
    cy.visit("/repos/");
    cy.byTestId("repository-overview-filter");
    cy.byTestId("primary-navigation-login").click();
    cy.byTestId("login-button");
  });
  it("Should login and direct to repositories overview", () => {
    cy.login("scmadmin", "scmadmin");

    cy.visit("/login");
    cy.byTestId("SCM-Administrator");
    cy.byTestId("primary-navigation-logout").click();
  });
  it("Should logout and direct to login page", () => {
    cy.login("scmadmin", "scmadmin");

    cy.visit("/repos/");
    cy.byTestId("repository-overview-filter");
    cy.byTestId("SCM-Administrator");
    cy.byTestId("primary-navigation-logout").click();
    cy.byTestId("login-button");
  });
  it("Anonymous user should not be able to change password", () => {
    cy.visit("/repos/");
    cy.byTestId("footer-user-profile").click();
    cy.byTestId("SCM-Anonymous");
    cy.containsNotByTestId("ul", "user-settings-link");
    cy.get("section").not("Change password");
  });

  after("Disable anonymous access", () => {
    cy.login("scmadmin", "scmadmin");
    setAnonymousMode("OFF");
    cy.byTestId("primary-navigation-logout").click();
  });
});

const setAnonymousMode = anonymousMode => {
  cy.byTestId("primary-navigation-admin").click();
  cy.byTestId("admin-settings-link").click();
  cy.byTestId("anonymous-mode-select")
    .select(anonymousMode)
    .should("have.value", anonymousMode);
  cy.byTestId("submit-button").click();
};
