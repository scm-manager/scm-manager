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

export default {
  files: [
    {
      oldPath: "CHANGELOG.md",
      newPath: "CHANGELOG.md",
      oldEndingNewLine: true,
      newEndingNewLine: true,
      oldRevision: "de732d6da1cc0be8454f004c14b7666c69c91fb4",
      newRevision: "148eb799f3a56909fe65b966e093a482ba542c81",
      type: "modify",
      language: "markdown",
      hunks: [
        {
          content: "@@ -5,7 +5,7 @@",
          oldStart: 5,
          newStart: 5,
          oldLines: 7,
          newLines: 7,
          changes: [
            {
              content: "The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),",
              type: "normal",
              oldLineNumber: 5,
              newLineNumber: 5,
              isNormal: true
            },
            {
              content: "and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).",
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
              content: "## Unreleased",
              type: "delete",
              lineNumber: 8,
              isDelete: true
            },
            {
              content: "## [2.7.1] - 2020-10-14",
              type: "insert",
              lineNumber: 8,
              isInsert: true
            },
            {
              content: "### Fixed",
              type: "normal",
              oldLineNumber: 9,
              newLineNumber: 9,
              isNormal: true
            },
            {
              content:
                "- Null Pointer Exception on anonymous migration with deleted repositories ([#1371](https://github.com/scm-manager/scm-manager/pull/1371))",
              type: "normal",
              oldLineNumber: 10,
              newLineNumber: 10,
              isNormal: true
            },
            {
              content:
                "- Null Pointer Exception on parsing SVN properties ([#1373](https://github.com/scm-manager/scm-manager/pull/1373))",
              type: "normal",
              oldLineNumber: 11,
              newLineNumber: 11,
              isNormal: true
            }
          ]
        }
      ],
      _links: {
        lines: {
          href:
            "http://localhost:8081/scm/api/v2/repositories/scmadmin/scm-manager/content/fbffdea2a566dc4ac54ea38d4aade5aaf541e7f2/CHANGELOG.md?start={start}&end={end}",
          templated: true
        }
      }
    }
  ],
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/v2/repositories/scmadmin/scm-manager/diff/fbffdea2a566dc4ac54ea38d4aade5aaf541e7f2/parsed"
    }
  }
};
