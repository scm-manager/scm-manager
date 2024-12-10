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

import { renderHook } from "@testing-library/react-hooks";
import { Repository } from "@scm-manager/ui-types";
import { binder } from "@scm-manager/ui-extensions";
import useDocumentTitle, { useDocumentTitleForRepository } from "./useDocumentTitle";

describe("useDocumentTitle", () => {
  it("should set document title", () => {
    renderHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Part1 - Part2 - SCM-Manager");
  });

  it("should append title if extension is a string", () => {
    binder.getExtension = () => ({ documentTitle: "myInstance" });
    renderHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Part1 - Part2 - SCM-Manager (myInstance)");
  });

  it("should modify title if extension is a function", () => {
    binder.getExtension = () => ({ documentTitle: (title: string) => `Modified: ${title}` });
    renderHook(() => useDocumentTitle("Part1", "Part2"));
    expect(document.title).toBe("Modified: Part1 - Part2 - SCM-Manager");
  });
});

describe("useDocumentTitleForRepository", () => {
  const repository: Repository = { namespace: "namespace", name: "name" } as Repository;

  it("should set the document title for a repository", () => {
    renderHook(() => useDocumentTitleForRepository(repository, "Part1", "Part2"));
    expect(document.title).toBe("Part1 - Part2 - namespace/name - SCM-Manager");
  });
});
