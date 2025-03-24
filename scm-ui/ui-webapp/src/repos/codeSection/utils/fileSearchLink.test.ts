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

import { File, Repository } from "@scm-manager/ui-types";
import { getFileSearchLink } from "./fileSearchLink";

describe("getFileSearchLink", () => {
  describe("git", () => {
    it("shouldCreateCorrectLinkFromGitRootDirectory", () => {
      const link = getFileSearchLink(repository, "main", baseUrl, rootDirectory);
      expect(link).toBe("repo/captain/kirk/code/search/main");
    });

    it("shouldCreateCorrectLinkFromGitFirstLevelDirectory", () => {
      const link = getFileSearchLink(repository, "main", baseUrl, firstLevelDirectory);
      expect(link).toBe("repo/captain/kirk/code/search/main?prevSourcePath=dir");
    });

    it("shouldCreateCorrectLinkFromGitSecondLevelDirectory", () => {
      const link = getFileSearchLink(repository, "main", baseUrl, secondLevelDirectory);
      expect(link).toBe("repo/captain/kirk/code/search/main?prevSourcePath=dir%2FsubDir");
    });

    const repository: Repository = {
      _links: {},
      name: "kirk",
      namespace: "captain",
      type: "git",
    };

    const firstLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "dir",
      path: "dir",
      revision: "main",
    };

    const secondLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "subDir",
      path: "dir/subDir",
      revision: "main",
    };

    const rootDirectory: File = {
      _links: {},
      directory: true,
      name: "",
      path: "",
      revision: "main",
    };

    const baseUrl = "repo/captain/kirk/code";
  });
  describe("svn", () => {
    it("shouldCreateCorrectLinkFromGitRootDirectory", () => {
      const link = getFileSearchLink(repository, "42", baseUrl, rootDirectory);
      expect(link).toBe("repo/subversion/space/code/search/42?prevSourcePath=42");
    });

    it("shouldCreateCorrectLinkFromGitFirstLevelDirectory", () => {
      const link = getFileSearchLink(repository, "0", baseUrl, firstLevelDirectory);
      expect(link).toBe("repo/subversion/space/code/search/0?prevSourcePath=0%2Ftrunk");
    });

    it("shouldCreateCorrectLinkFromGitSecondLevelDirectory", () => {
      const link = getFileSearchLink(repository, "42", baseUrl, secondLevelDirectory);
      expect(link).toBe("repo/subversion/space/code/search/42?prevSourcePath=42%2Ftrunk%2FsubDir");
    });

    const repository: Repository = {
      _links: {},
      name: "space",
      namespace: "subversion",
      type: "svn",
    };

    const firstLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "trunk",
      path: "trunk",
      revision: "0",
    };

    const secondLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "subDir",
      path: "trunk/subDir",
      revision: "42",
    };

    const rootDirectory: File = {
      _links: {},
      directory: true,
      name: "",
      path: "",
      revision: "42",
    };

    const baseUrl = "repo/subversion/space/code";
  });
  describe("hg", () => {
    it("shouldCreateCorrectLinkFromHgRootDirectory", () => {
      const link = getFileSearchLink(repository, "default", baseUrl, rootDirectory);
      expect(link).toBe("repo/heavy/metal/code/search/default");
    });

    it("shouldCreateCorrectLinkFromGitFirstLevelDirectory", () => {
      const link = getFileSearchLink(repository, "default", baseUrl, firstLevelDirectory);
      expect(link).toBe("repo/heavy/metal/code/search/default?prevSourcePath=dir");
    });

    it("shouldCreateCorrectLinkFromGitSecondLevelDirectory", () => {
      const link = getFileSearchLink(repository, "default", baseUrl, secondLevelDirectory);
      expect(link).toBe("repo/heavy/metal/code/search/default?prevSourcePath=dir%2FsubDir");
    });

    const repository: Repository = {
      _links: {},
      name: "metal",
      namespace: "heavy",
      type: "hg",
    };

    const firstLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "dir",
      path: "dir",
      revision: "default",
    };

    const secondLevelDirectory: File = {
      _links: {},
      directory: true,
      name: "subDir",
      path: "dir/subDir",
      revision: "default",
    };

    const rootDirectory: File = {
      _links: {},
      directory: true,
      name: "",
      path: "",
      revision: "default",
    };

    const baseUrl = "repo/heavy/metal/code";
  });
});
