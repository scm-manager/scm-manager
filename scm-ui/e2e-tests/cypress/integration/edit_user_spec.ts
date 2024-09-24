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
