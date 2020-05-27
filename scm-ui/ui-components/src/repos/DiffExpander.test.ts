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

import DiffExpander from "./DiffExpander";

const HUNK_0 = {
  content: "@@ -1,8 +1,8 @@",
  oldStart: 1,
  newStart: 1,
  oldLines: 8,
  newLines: 8,
  changes: [
    {
      content: "// @flow",
      type: "normal",
      oldLineNumber: 1,
      newLineNumber: 1,
      isNormal: true
    },
    {
      content: 'import React from "react";',
      type: "normal",
      oldLineNumber: 2,
      newLineNumber: 2,
      isNormal: true
    },
    {
      content: 'import { translate } from "react-i18next";',
      type: "delete",
      lineNumber: 3,
      isDelete: true
    },
    {
      content: 'import { Textarea } from "@scm-manager/ui-components";',
      type: "delete",
      lineNumber: 4,
      isDelete: true
    },
    {
      content: 'import type { Me } from "@scm-manager/ui-types";',
      type: "delete",
      lineNumber: 5,
      isDelete: true
    },
    {
      content: 'import {translate} from "react-i18next";',
      type: "insert",
      lineNumber: 3,
      isInsert: true
    },
    {
      content: 'import {Textarea} from "@scm-manager/ui-components";',
      type: "insert",
      lineNumber: 4,
      isInsert: true
    },
    {
      content: 'import type {Me} from "@scm-manager/ui-types";',
      type: "insert",
      lineNumber: 5,
      isInsert: true
    },
    {
      content: 'import injectSheet from "react-jss";',
      type: "normal",
      oldLineNumber: 6,
      newLineNumber: 6,
      isNormal: true
    },
    {
      content: "",
      type: "normal",
      oldLineNumber: 7,
      newLineNumber: 7,
      isNormal: true
    },
    {
      content: "const styles = {",
      type: "normal",
      oldLineNumber: 8,
      newLineNumber: 8,
      isNormal: true
    }
  ]
};
const HUNK_1 = {
  content: "@@ -14,6 +14,7 @@",
  oldStart: 14,
  newStart: 14,
  oldLines: 6,
  newLines: 7,
  changes: [
    {
      content: "type Props = {",
      type: "normal",
      oldLineNumber: 14,
      newLineNumber: 14,
      isNormal: true
    },
    {
      content: "  me: Me,",
      type: "normal",
      oldLineNumber: 15,
      newLineNumber: 15,
      isNormal: true
    },
    {
      content: "  onChange: string => void,",
      type: "normal",
      oldLineNumber: 16,
      newLineNumber: 16,
      isNormal: true
    },
    {
      content: "  disabled: boolean,",
      type: "insert",
      lineNumber: 17,
      isInsert: true
    },
    {
      content: "  //context props",
      type: "normal",
      oldLineNumber: 17,
      newLineNumber: 18,
      isNormal: true
    },
    {
      content: "  t: string => string,",
      type: "normal",
      oldLineNumber: 18,
      newLineNumber: 19,
      isNormal: true
    },
    {
      content: "  classes: any",
      type: "normal",
      oldLineNumber: 19,
      newLineNumber: 20,
      isNormal: true
    }
  ]
};
const HUNK_2 = {
  content: "@@ -21,7 +22,7 @@",
  oldStart: 21,
  newStart: 22,
  oldLines: 7,
  newLines: 7,
  changes: [
    {
      content: "",
      type: "normal",
      oldLineNumber: 21,
      newLineNumber: 22,
      isNormal: true
    },
    {
      content: "class CommitMessage extends React.Component<Props> {",
      type: "normal",
      oldLineNumber: 22,
      newLineNumber: 23,
      isNormal: true
    },
    {
      content: "  render() {",
      type: "normal",
      oldLineNumber: 23,
      newLineNumber: 24,
      isNormal: true
    },
    {
      content: "    const { t, classes, me, onChange } = this.props;",
      type: "delete",
      lineNumber: 24,
      isDelete: true
    },
    {
      content: "    const {t, classes, me, onChange, disabled} = this.props;",
      type: "insert",
      lineNumber: 25,
      isInsert: true
    },
    {
      content: "    return (",
      type: "normal",
      oldLineNumber: 25,
      newLineNumber: 26,
      isNormal: true
    },
    {
      content: "      <>",
      type: "normal",
      oldLineNumber: 26,
      newLineNumber: 27,
      isNormal: true
    },
    {
      content: "        <div className={classes.marginBottom}>",
      type: "normal",
      oldLineNumber: 27,
      newLineNumber: 28,
      isNormal: true
    }
  ]
};
const HUNK_3 = {
  content: "@@ -33,6 +34,7 @@",
  oldStart: 33,
  newStart: 34,
  oldLines: 6,
  newLines: 7,
  changes: [
    {
      content: "        <Textarea",
      type: "normal",
      oldLineNumber: 33,
      newLineNumber: 34,
      isNormal: true
    },
    {
      content: '          placeholder={t("scm-editor-plugin.commit.placeholder")}',
      type: "normal",
      oldLineNumber: 34,
      newLineNumber: 35,
      isNormal: true
    },
    {
      content: "          onChange={message => onChange(message)}",
      type: "normal",
      oldLineNumber: 35,
      newLineNumber: 36,
      isNormal: true
    },
    {
      content: "          disabled={disabled}",
      type: "insert",
      lineNumber: 37,
      isInsert: true
    },
    {
      content: "        />",
      type: "normal",
      oldLineNumber: 36,
      newLineNumber: 38,
      isNormal: true
    },
    {
      content: "      </>",
      type: "normal",
      oldLineNumber: 37,
      newLineNumber: 39,
      isNormal: true
    },
    {
      content: "    );",
      type: "normal",
      oldLineNumber: 38,
      newLineNumber: 40,
      isNormal: true
    }
  ]
};
const TEST_CONTENT = {
  oldPath: "src/main/js/CommitMessage.js",
  newPath: "src/main/js/CommitMessage.js",
  oldEndingNewLine: true,
  newEndingNewLine: true,
  oldRevision: "e05c8495bb1dc7505d73af26210c8ff4825c4500",
  newRevision: "4305a8df175b7bec25acbe542a13fbe2a718a608",
  type: "modify",
  language: "javascript",
  hunks: [HUNK_0, HUNK_1, HUNK_2, HUNK_3],
  _links: {
    lines: {
      href:
        "http://localhost:8081/scm/api/v2/repositories/scm-manager/scm-editor-plugin/content/f7a23064f3f2418f26140a9545559e72d595feb5/src/main/js/CommitMessage.js?start={start}?end={end}",
      templated: true
    }
  }
};

describe("diff expander", () => {
  const diffExpander = new DiffExpander(TEST_CONTENT);
  it("should have hunk count from origin", () => {
    expect(diffExpander.hunkCount()).toBe(4);
  });

  it("should return correct hunk", () => {
    expect(diffExpander.getHunk(1).hunk).toBe(HUNK_1);
  });
});
