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

describe("Repository File Search", () => {
  let username: string;
  let password: string;
  let namespace: string;
  let name: string;

  beforeEach(() => {
    // Create user and login
    username = hri.random();
    password = hri.random();
    cy.restSetConfig({ enabledFileSearch: true });
    cy.restCreateUser(username, password);
    cy.restLogin(username, password);

    // Create repo
    namespace = hri.random();
    name = hri.random();
    cy.restCreateRepo("git", namespace, name, true);
  });

  it("should search file inside repository", () => {
    // Arrange
    cy.restSetUserRepositoryRole(username, namespace, name, "WRITE");

    // Act
    cy.visit(`/repo/${namespace}/${name}/code/sources`);
    cy.byTestId("file_search_button").click();
    cy.url().should("include", `/repo/${namespace}/${name}/code/search/main?q=`);
    cy.byTestId("file_search_filter_input").type("README");

    // Assert
    cy.byTestId("file_search_single_result").contains("README.md");
  });
});
