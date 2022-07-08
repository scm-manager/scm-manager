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

describe("Edit User", () => {

  let userNameToEdit: string;

  beforeEach(() => {
    // Create user and login
    const username = hri.random();
    const password = hri.random();
    cy.restCreateUser(username, password);
    cy.restLogin(username, password);
    cy.restSetUserPermissions(username, ["user:*"]);

    userNameToEdit = hri.random();
    cy.restCreateUser(userNameToEdit, hri.random());
  });

  it("should edit user", () => {
    // Prepare data
    const newUser = hri.random();
    const newEmail = `${hri.random()}@${hri.random()}.com`;

    // Act
    cy.visit(`/user/${userNameToEdit}/settings/general`);
    cy.byTestId("input-displayname").clear().type(newUser);
    cy.byTestId("input-mail").clear().type(newEmail);
    cy.byTestId("submit-button").click();

    // Assert
    cy.get("div.notification.is-success").should("exist");
  });
});
