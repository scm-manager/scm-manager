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

describe("Anonymous Mode Disabled", () => {
  beforeEach(() => {
    cy.restSetAnonymousMode("OFF");
  });

  it("should show login page when not authenticated", () => {
    // Act
    cy.visit("/repos/");

    // Assert
    cy.byTestId("login-button");
  });

  it("should show footer for authenticated user", () => {
    // Arrange
    const username = hri.random();
    const password = hri.random();
    cy.restCreateUser(username, password);
    cy.restLogin(username, password);

    // Act
    cy.visit("/home");

    // Assert
    cy.byTestId("footer-user-profile");
  });
});
