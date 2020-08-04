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
    setUserPermissions("_anonymous", ["repository:read,pull:*"]);
    setAnonymousMode("OFF");

    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Please login to proceed");
    cy.get("div").not("Login");
    cy.get("div").not("Repositories");
  });
  it("Should redirect after login", () => {
    cy.visit("http://localhost:8081/scm/me/");
    cy.get("div.field.username > div > input").type("scmadmin");
    cy.get("div.field.password > div > input").type("scmadmin");
    cy.get("button")
      .contains("Login")
      .click();
    cy.contains("Profile");
  });
});

describe("With Anonymous mode protocol only enabled", () => {
  it("Should show login page without primary navigation", () => {
    setUserPermissions("_anonymous", ["repository:read,pull:*"]);
    setAnonymousMode("PROTOCOL_ONLY");

    cy.visit("http://localhost:8081/scm/repos/");
    cy.contains("Please login to proceed");
    cy.get("div").not("Login");
    cy.get("div").not("Repositories");
  });
});

describe("With Anonymous mode fully enabled", () => {
  it("Should show repositories overview with Login button in primary navigation", () => {
    setUserPermissions("_anonymous", ["repository:read,pull:*"]);
    setAnonymousMode("FULL");

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

const loginUser = (username, password) => {
  const loginUrl = `http://localhost:8081/scm/api/v2/auth/access_token`;

  cy.request({
    method: "POST",
    url: loginUrl,
    body: {
      cookie: true,
      username: username,
      password: password,
      grantType: "password"
    }
  });
};

const setUserPermissions = (user, permissions) => {
  const MEDIA_TYPE = "application/vnd.scmm-permissionCollection+json;v=2";
  const userPermissionUrl = `http://localhost:8081/scm/api/v2/users/${user}/permissions`;

  cy.request({
    method: "PUT",
    url: userPermissionUrl,
    body: { permissions: permissions },
    headers: { "content-type": MEDIA_TYPE },
    auth: {
      user: "scmadmin",
      pass: "scmadmin",
      sendImmediately: true
    }
  });
};

const setAnonymousMode = anonMode => {
  const MEDIA_TYPE = "application/vnd.scmm-config+json;v=2";

  const content = {
    adminGroups: [],
    adminUsers: [],
    anonymousMode: anonMode,
    baseUrl: "http://localhost:8081/scm",
    dateFormat: "YYYY-MM-DD HH:mm:ss",
    disableGroupingGrid: false,
    enableProxy: false,
    enabledXsrfProtection: false,
    forceBaseUrl: false,
    loginAttemptLimit: 100,
    loginAttemptLimitTimeout: 300,
    loginInfoUrl: "https://login-info.scm-manager.org/api/v1/login-info",
    namespaceStrategy: "UsernameNamespaceStrategy",
    pluginUrl:
      "https://oss.cloudogu.com/jenkins/job/scm-manager/job/scm-manager-bitbucket/job/plugin-snapshot/job/master/lastSuccessfulBuild/artifact/plugins/plugin-center.json",
    proxyExcludes: [],
    proxyPassword: null,
    proxyPort: 8080,
    proxyServer: "proxy.mydomain.com",
    proxyUser: null,
    realmDescription: "SONIA :: SCM Manager",
    skipFailedAuthenticators: false
  };

  cy.request({
    method: "PUT",
    url: "http://localhost:8081/scm/api/v2/config",
    body: content,
    headers: { "content-type": MEDIA_TYPE },
    auth: {
      user: "scmadmin",
      pass: "scmadmin",
      sendImmediately: true
    }
  });
};
