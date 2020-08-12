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
    cy.setAnonymousMode("OFF");
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
