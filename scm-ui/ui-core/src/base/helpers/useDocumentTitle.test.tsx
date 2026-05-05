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

import { Repository } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";
import useDocumentTitle, { useDocumentTitleForRepository } from "./useDocumentTitle";
import { renderTestHook } from "./devbuild";

describe("useDocumentTitle", () => {
  it("should set document title", () => {
    renderTestHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Part1 - Part2 - documentTitle.suffix");
  });

  it("should append title if extension is a string", () => {
    binder.getExtension = () => ({ documentTitle: "myInstance" });
    renderTestHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Part1 - Part2 - documentTitle.suffix (myInstance)");
  });

  it("should modify title if extension is a function", () => {
    binder.getExtension = () => ({ documentTitle: (title: string) => `Modified: ${title}` });
    renderTestHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Modified: Part1 - Part2 - documentTitle.suffix");
  });
});

describe("useDocumentTitleForRepository", () => {
  const repository: Repository = { namespace: "namespace", name: "name" } as Repository;

  it("should set the document title for a repository", () => {
    binder.getExtension = () => ({ documentTitle: (title: string) => `Repository: ${title}` });
    renderTestHook(() => useDocumentTitleForRepository(repository, "Part1", "Part2"));
    expect(document.title).toBe("Repository: Part1 - Part2 - namespace/name - documentTitle.suffix");
  });
});
