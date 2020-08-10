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
  it("Should show login page without primary navigation", () => {
    loginUser("scmadmin", "scmadmin");
    setAnonymousMode("OFF");

    cy.get("li")
      .contains("Logout")
      .click();
    cy.contains("Please login to proceed");
    cy.get("div").not("Login");
    cy.get("div").not("Repositories");
  });
  it("Should redirect after login", () => {
    loginUser("scmadmin", "scmadmin");

    cy.visit("http://localhost:8081/scm/me");
    cy.contains("Profile");
    cy.get("li")
      .contains("Logout")
      .click();
  });
});

describe("With Anonymous mode protocol only enabled", () => {
  it("Should show login page without primary navigation", () => {
    loginUser("scmadmin", "scmadmin");
    setAnonymousMode("PROTOCOL_ONLY");

    // Give anonymous user permissions
    cy.get("li")
      .contains("Users")
      .click();
    cy.get("td")
      .contains("_anonymous")
      .click();
    cy.get("a")
      .contains("Settings")
      .click();
    cy.get("li")
      .contains("Permissions")
      .click();
    cy.get("label")
      .contains("Read all repositories")
      .click();
    cy.get("button")
      .contains("Set permissions")
      .click();

    cy.get("li")
      .contains("Logout")
      .click();
    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Please login to proceed");
    cy.get("div").not("Login");
    cy.get("div").not("Repositories");
  });
});

describe("With Anonymous mode fully enabled", () => {
  it("Should show repositories overview with Login button in primary navigation", () => {
    loginUser("scmadmin", "scmadmin");
    setAnonymousMode("FULL");

    cy.get("li")
      .contains("Logout")
      .click();
    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Overview of available repositories");
    cy.contains("SCM Anonymous");
    cy.get("ul").contains("Login");
  });
  it("Should show login page on url", () => {
    cy.visit("http://localhost:8081/scm/login/");
  });
  it("Should show login page on link click", () => {
    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Overview of available repositories");
    cy.contains("Login").click();
    cy.contains("Please login to proceed");
  });
  it("Should login and direct to repositories overview", () => {
    loginUser("scmadmin", "scmadmin");

    cy.visit("http://localhost:8081/scm/login");
    cy.contains("SCM Administrator");
    cy.get("li")
      .contains("Logout")
      .click();
  });
  it("Should logout and direct to login page", () => {
    loginUser("scmadmin", "scmadmin");

    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Overview of available repositories");
    cy.contains("SCM Administrator");
    cy.contains("Logout").click();
    cy.contains("Please login to proceed");
  });
  it("Anonymous user should not be able to change password", () => {
    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Profile").click();
    cy.contains("scm-anonymous@scm-manager.org");
    cy.get("ul").not("Settings");
    cy.get("section").not("Change password");
  });
});

describe("Disable anonymous mode after tests", () => {
  it("Disable anonymous mode after tests", () => {
    loginUser("scmadmin", "scmadmin");
    setAnonymousMode("OFF");

    cy.get("li")
      .contains("Logout")
      .click();
  });
});

const setAnonymousMode = anonymousMode => {
  cy.get("li")
    .contains("Administration")
    .click();
  cy.get("li")
    .contains("Settings")
    .click();
  cy.get("select")
    .contains("Disabled")
    .parent()
    .select(anonymousMode)
    .should("have.value", anonymousMode);
  cy.get("button")
    .contains("Submit")
    .click();
};

const loginUser = (username, password) => {
  cy.visit("http://localhost:8081/scm/login");
  cy.get("div.field.username > div > input").type(username);
  cy.get("div.field.password > div > input").type(password);
  cy.get("button")
    .contains("Login")
    .click();
};
