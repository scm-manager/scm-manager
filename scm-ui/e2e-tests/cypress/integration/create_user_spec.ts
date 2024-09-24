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

describe("Create User", () => {
  beforeEach(() => {
    // Create user and login
    const username = hri.random();
    const password = hri.random();
    cy.restCreateUser(username, password);
    cy.restLogin(username, password);
    cy.restSetUserPermissions(username, ["user:*"]);
  });

  it("should create new user", () => {
    // Prepare data
    const newUser = hri.random();
    const password = hri.random();

    // Act
    cy.visit("/users/create");
    cy.byTestId("input-username").clear().type(newUser).should("have.value", newUser);
    cy.byTestId("input-displayname").type(newUser);
    cy.byTestId("input-password").type(password);
    cy.byTestId("input-password-confirmation").type(password);
    cy.byTestId("submit-button").click();

    // Assert
    cy.url().should("include", `/user/${newUser}`);
  });
});
