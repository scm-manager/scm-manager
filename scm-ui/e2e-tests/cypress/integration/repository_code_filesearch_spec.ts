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

describe("Repository File Search", () => {
  let username: string;
  let password: string;
  let namespace: string;
  let name: string;

  beforeEach(() => {
    // Create user and login
    username = hri.random();
    password = hri.random();
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
