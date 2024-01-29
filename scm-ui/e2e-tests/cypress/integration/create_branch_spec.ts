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
