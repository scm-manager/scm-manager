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

import fetchMock from "fetch-mock-jest";
import { Diff } from "@scm-manager/ui-types";
import { act, renderHook } from "@testing-library/react-hooks";
import { useDiff } from "./diff";
import createWrapper from "./tests/createWrapper";

describe("Test diff", () => {
  const simpleDiff: Diff = {
    files: [
      {
        oldPath: "/dev/null",
        newPath: "0.txt",
        oldEndingNewLine: true,
        newEndingNewLine: true,
        oldRevision: "0000000000000000000000000000000000000000",
        newRevision: "573541ac9702dd3969c9bc859d2b91ec1f7e6e56",
        type: "add",
        language: "text",
        hunks: [
          {
            content: "@@ -0,0 +1 @@",
            newStart: 1,
            newLines: 1,
            changes: [
              {
                content: "0",
                type: "insert",
                lineNumber: 1,
                isInsert: true,
              },
            ],
          },
        ],
        _links: {
          lines: {
            href: "/api/v2/repositories/scmadmin/HeartOfGold-git/content/one_to_onehundred/0.txt?start={start}&end={end}",
            templated: true,
          },
        },
      },
    ],
    partial: false,
    _links: {
      self: {
        href: "/api/v2/diff",
      },
    },
  };

  const partialDiff1: Diff = {
    files: [
      {
        oldPath: "/dev/null",
        newPath: "0.txt",
        oldEndingNewLine: true,
        newEndingNewLine: true,
        oldRevision: "0000000000000000000000000000000000000000",
        newRevision: "573541ac9702dd3969c9bc859d2b91ec1f7e6e56",
        type: "add",
        language: "text",
        hunks: [
          {
            content: "@@ -0,0 +1 @@",
            newStart: 1,
            newLines: 1,
            changes: [
              {
                content: "0",
                type: "insert",
                lineNumber: 1,
                isInsert: true,
              },
            ],
          },
        ],
        _links: {
          lines: {
            href: "/api/v2/repositories/scmadmin/HeartOfGold-git/content/one_to_onehundred/0.txt?start={start}&end={end}",
            templated: true,
          },
        },
      },
    ],
    partial: true,
    _links: {
      self: {
        href: "/diff",
      },
      next: {
        href: "/diff?offset=1&limit=1",
      },
    },
  };

  const partialDiff2: Diff = {
    files: [
      {
        oldPath: "/dev/null",
        newPath: "1.txt",
        oldEndingNewLine: true,
        newEndingNewLine: true,
        oldRevision: "0000000000000000000000000000000000000000",
        newRevision: "573541ac9702dd3969c9bc859d2b91ec1f7e6e56",
        type: "add",
        language: "text",
        hunks: [
          {
            content: "@@ -0,0 +1 @@",
            newStart: 1,
            newLines: 1,
            changes: [
              {
                content: "1",
                type: "insert",
                lineNumber: 1,
                isInsert: true,
              },
            ],
          },
        ],
        _links: {
          lines: {
            href: "/api/v2/repositories/scmadmin/HeartOfGold-git/content/one_to_onehundred/1.txt?start={start}&end={end}",
            templated: true,
          },
        },
      },
    ],
    partial: false,
    _links: {
      self: {
        href: "/diff",
      },
    },
  };

  beforeEach(() => {
    fetchMock.reset();
  });

  it("should return simple parsed diff", async () => {
    fetchMock.getOnce("/api/v2/diff", {
      body: simpleDiff,
      headers: { "Content-Type": "application/vnd.scmm-diffparsed+json;v=2" },
    });
    const { result, waitFor } = renderHook(() => useDiff("/diff"), {
      wrapper: createWrapper(),
    });
    await waitFor(() => !!result.current.data);
    expect(result.current.data).toEqual(simpleDiff);
  });

  it("should parse and return textual diff", async () => {
    fetchMock.getOnce("/api/v2/diff", {
      body: `diff --git a/new.txt b/new.txt
--- a/new.txt
+++ b/new.txt
@@ -1,1 +1,1 @@
-i am old!
\\ No newline at end of file
+i am new!
\\ No newline at end of file
`,
      headers: { "Content-Type": "text/plain" },
    });
    const { result, waitFor } = renderHook(() => useDiff("/diff"), {
      wrapper: createWrapper(),
    });
    await waitFor(() => !!result.current.data);
    expect(result.current.data?.files).toHaveLength(1);
  });

  it("should return parsed diff in multiple chunks", async () => {
    fetchMock.getOnce("/api/v2/diff?limit=1", {
      body: partialDiff1,
      headers: { "Content-Type": "application/vnd.scmm-diffparsed+json;v=2" },
    });
    fetchMock.getOnce("/api/v2/diff?offset=1&limit=1", {
      body: partialDiff2,
      headers: { "Content-Type": "application/vnd.scmm-diffparsed+json;v=2" },
    });
    const { result, waitFor, waitForNextUpdate } = renderHook(() => useDiff("/diff?limit=1"), {
      wrapper: createWrapper(),
    });
    await waitFor(() => !!result.current.data);
    expect(result.current.data).toEqual(partialDiff1);

    await act(() => {
      const { fetchNextPage } = result.current;
      fetchNextPage();
      return waitForNextUpdate();
    });

    await waitFor(() => !result.current.isFetchingNextPage);

    expect(result.current.data).toEqual({ ...partialDiff2, files: [partialDiff1.files[0], partialDiff2.files[0]] });
  });

  it("should append query parameters to url which has already query params", async () => {
    fetchMock.getOnce("/api/v2/diff?format=GIT&limit=25&ignoreWhitespace=NONE", {
      body: simpleDiff,
      headers: { "Content-Type": "application/vnd.scmm-diffparsed+json;v=2" },
    });
    const { result, waitFor } = renderHook(() => useDiff("/diff?format=GIT", { limit: 25, ignoreWhitespace: "NONE" }), {
      wrapper: createWrapper(),
    });
    await waitFor(() => !!result.current.data);
    expect(result.current.data).toEqual(simpleDiff);
  });
});
