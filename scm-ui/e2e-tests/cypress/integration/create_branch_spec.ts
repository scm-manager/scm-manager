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

describe("Create Branch", () => {
  const username = hri.random();
  const password = hri.random();
  const repoNamespace = hri.random();
  const repoName = hri.random();

  before(() => {
    cy.restCreateUser(username, password);
    cy.restCreateRepo("git", repoNamespace, repoName, true);
    cy.restSetUserPermissions(username, ["repository:*"]);
  })

  beforeEach(() => {
    // Create user and login
    cy.restLogin(username, password);
  });

  [
    {randomPrefix: hri.random(), branchName: "develop", expectedBranchUrlFragment: "develop"},
    {randomPrefix: hri.random(), branchName: "feature/e2e", expectedBranchUrlFragment: "feature%2Fe2e"},
    {randomPrefix: hri.random(), branchName: "feature%2Fe2e", expectedBranchUrlFragment: "feature%252Fe2e"},
    {randomPrefix: hri.random(), branchName: "Да", expectedBranchUrlFragment: "%D0%94%D0%B0"},
    {randomPrefix: hri.random(), branchName: "はい", expectedBranchUrlFragment: "%E3%81%AF%E3%81%84"},
    {randomPrefix: hri.random(), branchName: "%3F", expectedBranchUrlFragment: "%253F"},
    {randomPrefix: hri.random(), branchName: "%", expectedBranchUrlFragment: "%25"},
    {randomPrefix: hri.random(), branchName: "%%", expectedBranchUrlFragment: "%25%25"},
    {randomPrefix: hri.random(), branchName: "%25", expectedBranchUrlFragment: "%2525"},
    {randomPrefix: hri.random(), branchName: "&", expectedBranchUrlFragment: "%26"},
    {randomPrefix: hri.random(), branchName: "&&", expectedBranchUrlFragment: "%26%26"},
    {randomPrefix: hri.random(), branchName: "&26", expectedBranchUrlFragment: "%2626"},
    {randomPrefix: hri.random(), branchName: "%&", expectedBranchUrlFragment: "%25%26"},
  ].forEach(data => {
    it(`should create branch ${data.branchName} with url ${data.expectedBranchUrlFragment}`, () => {
      cy.visit(`/repo/${repoNamespace}/${repoName}/branches/create`);
      cy.byTestId("input-branch-name").type(`${data.randomPrefix}-${data.branchName}`);
      cy.byTestId("submit-button").click();

      cy.contains(data.branchName).should("be.visible");
      cy.url().should("include", `/repo/${repoNamespace}/${repoName}/branch/${data.randomPrefix}-${data.expectedBranchUrlFragment}`);
    });
  })

  it("should not create branch with trailing slash", () => {
    cy.visit(`/repo/${repoNamespace}/${repoName}/branches/create`);
    cy.byTestId("input-branch-name").type("/");
    cy.byTestId("submit-button").click();

    cy.contains("1wR7ZBe7H1").should("be.visible");
  })

  it("? should not be allowed in branch name", () => {
    cy.visit(`/repo/${repoNamespace}/${repoName}/branches/create`);
    cy.byTestId("input-branch-name").type("?");

    cy.byTestId("submit-button").should("be.disabled");
  })
});
