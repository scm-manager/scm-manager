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

import fetchMock from "fetch-mock";
import DiffExpander from "./DiffExpander";
import { FileDiff, Hunk } from "@scm-manager/ui-types";

const HUNK_0: Hunk = {
  content: "@@ -1,8 +1,8 @@",
  oldStart: 1,
  newStart: 1,
  oldLines: 8,
  newLines: 8,
  changes: [
    { content: "line", type: "normal", oldLineNumber: 1, newLineNumber: 1, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 2, newLineNumber: 2, isNormal: true },
    { content: "line", type: "delete", lineNumber: 3, isDelete: true },
    { content: "line", type: "delete", lineNumber: 4, isDelete: true },
    { content: "line", type: "delete", lineNumber: 5, isDelete: true },
    { content: "line", type: "insert", lineNumber: 3, isInsert: true },
    { content: "line", type: "insert", lineNumber: 4, isInsert: true },
    { content: "line", type: "insert", lineNumber: 5, isInsert: true },
    { content: "line", type: "normal", oldLineNumber: 6, newLineNumber: 6, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 7, newLineNumber: 7, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 8, newLineNumber: 8, isNormal: true },
  ],
};
const HUNK_1: Hunk = {
  content: "@@ -14,6 +14,7 @@",
  oldStart: 14,
  newStart: 14,
  oldLines: 6,
  newLines: 7,
  changes: [
    { content: "line", type: "normal", oldLineNumber: 14, newLineNumber: 14, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 15, newLineNumber: 15, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 16, newLineNumber: 16, isNormal: true },
    { content: "line", type: "insert", lineNumber: 17, isInsert: true },
    { content: "line", type: "normal", oldLineNumber: 17, newLineNumber: 18, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 18, newLineNumber: 19, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 19, newLineNumber: 20, isNormal: true },
  ],
};
const HUNK_2: Hunk = {
  content: "@@ -21,7 +22,7 @@",
  oldStart: 21,
  newStart: 22,
  oldLines: 7,
  newLines: 7,
  changes: [
    { content: "line", type: "normal", oldLineNumber: 21, newLineNumber: 22, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 22, newLineNumber: 23, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 23, newLineNumber: 24, isNormal: true },
    { content: "line", type: "delete", lineNumber: 24, isDelete: true },
    { content: "line", type: "insert", lineNumber: 25, isInsert: true },
    { content: "line", type: "normal", oldLineNumber: 25, newLineNumber: 26, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 26, newLineNumber: 27, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 27, newLineNumber: 28, isNormal: true },
  ],
};
const HUNK_3: Hunk = {
  content: "@@ -33,6 +34,7 @@",
  oldStart: 33,
  newStart: 34,
  oldLines: 6,
  newLines: 7,
  changes: [
    { content: "line", type: "normal", oldLineNumber: 33, newLineNumber: 34, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 34, newLineNumber: 35, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 35, newLineNumber: 36, isNormal: true },
    { content: "line", type: "insert", lineNumber: 37, isInsert: true },
    { content: "line", type: "normal", oldLineNumber: 36, newLineNumber: 38, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 37, newLineNumber: 39, isNormal: true },
    { content: "line", type: "normal", oldLineNumber: 38, newLineNumber: 40, isNormal: true },
  ],
};
const TEST_CONTENT_WITH_HUNKS: FileDiff = {
  hunks: [HUNK_0, HUNK_1, HUNK_2, HUNK_3],
  newEndingNewLine: true,
  newPath: "src/main/js/CommitMessage.js",
  newRevision: "4305a8df175b7bec25acbe542a13fbe2a718a608",
  oldEndingNewLine: true,
  oldPath: "src/main/js/CommitMessage.js",
  oldRevision: "e05c8495bb1dc7505d73af26210c8ff4825c4500",
  type: "modify",
  language: "javascript",
  _links: {
    lines: {
      href: "http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start={start}&end={end}",
      templated: true,
    },
  },
};

const TEST_CONTENT_WITH_NEW_BINARY_FILE: FileDiff = {
  oldPath: "/dev/null",
  newPath: "src/main/fileUploadV2.png",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "0000000000000000000000000000000000000000",
  newRevision: "86c370aae0727d628a5438f79a5cdd45752b9d99",
  type: "add",
};

const TEST_CONTENT_WITH_NEW_TEXT_FILE: FileDiff = {
  oldPath: "/dev/null",
  newPath: "src/main/markdown/README.md",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "0000000000000000000000000000000000000000",
  newRevision: "4e173d365d796b9a9e7562fcd0ef90398ae37046",
  type: "add",
  language: "markdown",
  hunks: [
    {
      content: "@@ -0,0 +1,2 @@",
      newStart: 1,
      newLines: 2,
      changes: [
        { content: "line 1", type: "insert", lineNumber: 1, isInsert: true },
        { content: "line 2", type: "insert", lineNumber: 2, isInsert: true },
      ],
    },
  ],
  _links: {
    lines: {
      href: "http://localhost:8081/scm/api/v2/repositories/scm-manager/scm-editor-plugin/content/c63898d35520ee47bcc3a8291660979918715762/src/main/markdown/README.md?start={start}&end={end}",
      templated: true,
    },
  },
};

const TEST_CONTENT_WITH_DELETED_TEXT_FILE: FileDiff = {
  oldPath: "README.md",
  newPath: "/dev/null",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "4875ab3b7a1bb117e1948895148557fc5c0b6f75",
  newRevision: "0000000000000000000000000000000000000000",
  type: "delete",
  language: "markdown",
  hunks: [
    {
      content: "@@ -1 +0,0 @@",
      oldStart: 1,
      oldLines: 1,
      changes: [{ content: "# scm-editor-plugin", type: "delete", lineNumber: 1, isDelete: true }],
    },
  ],
  _links: { lines: { href: "http://localhost:8081/dev/null?start={start}&end={end}", templated: true } },
};

const TEST_CONTENT_WITH_DELETED_LINES_AT_END: FileDiff = {
  oldPath: "pom.xml",
  newPath: "pom.xml",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "b207512c0eab22536c9e5173afbe54cc3a24a22e",
  newRevision: "5347c3fe0c2c4d4de7f308ae61bd5546460d7e93",
  type: "modify",
  language: "xml",
  hunks: [
    {
      content: "@@ -108,15 +108,3 @@",
      oldStart: 108,
      newStart: 108,
      oldLines: 15,
      newLines: 3,
      changes: [
        { content: "line", type: "normal", oldLineNumber: 108, newLineNumber: 108, isNormal: true },
        { content: "line", type: "normal", oldLineNumber: 109, newLineNumber: 109, isNormal: true },
        { content: "line", type: "normal", oldLineNumber: 110, newLineNumber: 110, isNormal: true },
        { content: "line", type: "delete", lineNumber: 111, isDelete: true },
        { content: "line", type: "delete", lineNumber: 112, isDelete: true },
        { content: "line", type: "delete", lineNumber: 113, isDelete: true },
        { content: "line", type: "delete", lineNumber: 114, isDelete: true },
        { content: "line", type: "delete", lineNumber: 115, isDelete: true },
        { content: "line", type: "delete", lineNumber: 116, isDelete: true },
        { content: "line", type: "delete", lineNumber: 117, isDelete: true },
        { content: "line", type: "delete", lineNumber: 118, isDelete: true },
        { content: "line", type: "delete", lineNumber: 119, isDelete: true },
        { content: "line", type: "delete", lineNumber: 120, isDelete: true },
        { content: "line", type: "delete", lineNumber: 121, isDelete: true },
        { content: "line", type: "delete", lineNumber: 122, isDelete: true },
      ],
    },
  ],
  _links: {
    lines: {
      href: "http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start={start}&end={end}",
      templated: true,
    },
  },
};

const TEST_CONTENT_WITH_ALL_LINES_REMOVED_FROM_FILE: FileDiff = {
  oldPath: "pom.xml",
  newPath: "pom.xml",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "2cc811c64f71ceda28f1ec0d97e1973395b299ff",
  newRevision: "e69de29bb2d1d6434b8b29ae775ad8c2e48c5391",
  type: "modify",
  language: "xml",
  hunks: [
    {
      content: "@@ -1,3 +0,0 @@",
      oldStart: 1,
      oldLines: 3,
      changes: [
        { content: "line", type: "delete", lineNumber: 1, isDelete: true },
        { content: "line", type: "delete", lineNumber: 2, isDelete: true },
        { content: "line", type: "delete", lineNumber: 3, isDelete: true },
      ],
    },
  ],
  _links: {
    lines: {
      href: "http://localhost:8081/scm/api/v2/repositories/scm-manager/scm-editor-plugin/content/b313a7690f028c77df98417c1ed6cba67e5692ec/pom.xml?start={start}&end={end}",
      templated: true,
    },
  },
};

describe("with hunks the diff expander", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_HUNKS);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should have hunk count from origin", () => {
    expect(diffExpander.hunkCount()).toBe(4);
  });

  it("should return correct hunk", () => {
    expect(diffExpander.getHunk(1).hunk).toBe(HUNK_1);
  });

  it("should return max expand head range for first hunk", () => {
    expect(diffExpander.getHunk(0).maxExpandHeadRange).toBe(0);
  });

  it("should return max expand head range for hunks in the middle", () => {
    expect(diffExpander.getHunk(1).maxExpandHeadRange).toBe(5);
  });

  it("should return max expand bottom range for hunks in the middle", () => {
    expect(diffExpander.getHunk(1).maxExpandBottomRange).toBe(1);
  });

  it("should return a really bix number for the expand bottom range of the last hunk", () => {
    expect(diffExpander.getHunk(3).maxExpandBottomRange).toBe(-1);
  });
  it("should create new hunk with new line from api client at the bottom", async () => {
    expect(diffExpander.getHunk(1).hunk.changes.length).toBe(7);
    const oldHunkCount = diffExpander.hunkCount();
    const expandedHunk = diffExpander.getHunk(1).hunk;
    const subsequentHunk = diffExpander.getHunk(2).hunk;
    fetchMock.get("http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start=20&end=21", "new line 1");
    let newFile: FileDiff;
    await diffExpander
      .getHunk(1)
      .expandBottom(1)
      .then((file) => (newFile = file));
    expect(fetchMock.done()).toBe(true);
    expect(newFile!.hunks!.length).toBe(oldHunkCount + 1);
    expect(newFile!.hunks![1]).toBe(expandedHunk);

    const newHunk = newFile!.hunks![2];
    expect(newHunk.changes.length).toBe(1);
    expect(newHunk.changes[0].content).toBe("new line 1");
    expect(newHunk.expansion).toBe(true);

    expect(newFile!.hunks![3]).toBe(subsequentHunk);
  });
  it("should create new hunk with new line from api client at the top", async () => {
    expect(diffExpander.getHunk(1).hunk.changes.length).toBe(7);
    const oldHunkCount = diffExpander.hunkCount();
    const expandedHunk = diffExpander.getHunk(1).hunk;
    const preceedingHunk = diffExpander.getHunk(0).hunk;
    fetchMock.get(
      "http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start=8&end=13",
      "new line 9\nnew line 10\nnew line 11\nnew line 12\nnew line 13"
    );
    let newFile: FileDiff;
    await diffExpander
      .getHunk(1)
      .expandHead(5)
      .then((file) => (newFile = file));
    expect(fetchMock.done()).toBe(true);
    expect(newFile!.hunks!.length).toBe(oldHunkCount + 1);
    expect(newFile!.hunks![0]).toBe(preceedingHunk);
    expect(newFile!.hunks![2]).toBe(expandedHunk);

    const newHunk = newFile!.hunks![1];
    expect(newHunk.changes.length).toBe(5);
    expect(newHunk.changes[0].content).toBe("new line 9");
    expect(newHunk.changes[0].oldLineNumber).toBe(9);
    expect(newHunk.changes[0].newLineNumber).toBe(9);
    expect(newHunk.changes[1].content).toBe("new line 10");
    expect(newHunk.changes[1].oldLineNumber).toBe(10);
    expect(newHunk.changes[1].newLineNumber).toBe(10);
    expect(newHunk.changes[4].content).toBe("new line 13");
    expect(newHunk.changes[4].oldLineNumber).toBe(13);
    expect(newHunk.changes[4].newLineNumber).toBe(13);
    expect(newHunk.expansion).toBe(true);
  });
  it("should set fully expanded to true if expanded completely", async () => {
    const oldHunkCount = diffExpander.hunkCount();
    fetchMock.get(
      "http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start=40&end=50",
      "new line 40\nnew line 41\nnew line 42"
    );
    let newFile: FileDiff;
    await diffExpander
      .getHunk(3)
      .expandBottom(10)
      .then((file) => (newFile = file));
    expect(newFile!.hunks!.length).toBe(oldHunkCount + 1);
    expect(newFile!.hunks![4].fullyExpanded).toBe(true);
  });
  it("should set end to -1 if requested to expand to the end", async () => {
    fetchMock.get(
      "http://localhost:8081/scm/api/v2/content/abc/CommitMessage.js?start=40&end=-1",
      "new line 40\nnew line 41\nnew line 42"
    );
    let newFile: FileDiff;
    await diffExpander
      .getHunk(3)
      .expandBottom(-1)
      .then((file) => (newFile = file));
    await fetchMock.flush(true);
    expect(newFile!.hunks![4].fullyExpanded).toBe(true);
  });
});

describe("for a new file with text input the diff expander", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_NEW_TEXT_FILE);
  it("should create answer for single hunk", () => {
    expect(diffExpander.hunkCount()).toBe(1);
  });
  it("should neither give expandable lines for top nor bottom", () => {
    const hunk = diffExpander.getHunk(0);
    expect(hunk.maxExpandHeadRange).toBe(0);
    expect(hunk.maxExpandBottomRange).toBe(0);
  });
});

describe("for a deleted file with text input the diff expander", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_DELETED_TEXT_FILE);
  it("should create answer for single hunk", () => {
    expect(diffExpander.hunkCount()).toBe(1);
  });
  it("should neither give expandable lines for top nor bottom", () => {
    const hunk = diffExpander.getHunk(0);
    expect(hunk.maxExpandHeadRange).toBe(0);
    expect(hunk.maxExpandBottomRange).toBe(0);
  });
});

describe("for a new file with binary input the diff expander", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_NEW_BINARY_FILE);
  it("should create answer for no hunk", () => {
    expect(diffExpander.hunkCount()).toBe(0);
  });
});

describe("with deleted lines at the end", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_DELETED_LINES_AT_END);
  it("should not be expandable", () => {
    expect(diffExpander.getHunk(0)!.maxExpandBottomRange).toBe(0);
  });
});

describe("with all lines removed from file", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT_WITH_ALL_LINES_REMOVED_FROM_FILE);
  it("should not be expandable", () => {
    expect(diffExpander.getHunk(0)!.maxExpandBottomRange).toBe(0);
  });
});
