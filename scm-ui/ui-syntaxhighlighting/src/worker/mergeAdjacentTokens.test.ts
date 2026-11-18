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

import { HunkData, tokenize, TokenNode } from "react-diff-view";
import { refractor } from "refractor/lib/core";
import java from "refractor/lang/java.js";
import mergeAdjacentTokens, { mergeAdjacentNodes } from "./mergeAdjacentTokens";

const text = (value: string): TokenNode => ({ type: "text", value });

const span = (className: string[], ...children: TokenNode[]): TokenNode => ({
  type: "element",
  tagName: "span",
  properties: { className },
  children,
});

describe("mergeAdjacentNodes", () => {
  it("should concatenate adjacent text nodes", () => {
    expect(mergeAdjacentNodes([text("pack"), text("age")])).toEqual([text("package")]);
  });

  it("should combine adjacent elements with equal properties", () => {
    const nodes = [span(["token", "keyword"], text("pack")), span(["token", "keyword"], text("age"))];

    expect(mergeAdjacentNodes(nodes)).toEqual([span(["token", "keyword"], text("package"))]);
  });

  it("should not combine elements with different properties", () => {
    const nodes = [span(["token", "keyword"], text("import")), text(" "), span(["token", "namespace"], text("com"))];

    expect(mergeAdjacentNodes(nodes)).toEqual(nodes);
  });

  it("should not combine elements of different tags", () => {
    const nodes: TokenNode[] = [
      { type: "element", tagName: "mark", properties: {}, children: [text("a")] },
      { type: "element", tagName: "span", properties: {}, children: [text("b")] },
    ];

    expect(mergeAdjacentNodes(nodes)).toEqual(nodes);
  });

  it("should not combine elements without children", () => {
    const nodes: TokenNode[] = [
      { type: "element", tagName: "br", properties: {} },
      { type: "element", tagName: "br", properties: {} },
    ];

    expect(mergeAdjacentNodes(nodes)).toEqual(nodes);
  });

  it("should merge the children of combined elements", () => {
    const nodes = [
      span(["token", "import"], span(["token", "namespace"], text("com"))),
      span(["token", "import"], span(["token", "namespace"], span(["token", "punctuation"], text(".")))),
      span(["token", "import"], span(["token", "namespace"], text("cloudogu"))),
      span(["token", "import"], span(["token", "class-name"], text("BasicComment"))),
    ];

    expect(mergeAdjacentNodes(nodes)).toEqual([
      span(
        ["token", "import"],
        span(["token", "namespace"], text("com"), span(["token", "punctuation"], text(".")), text("cloudogu")),
        span(["token", "class-name"], text("BasicComment")),
      ),
    ]);
  });

  it("should restore whitespace markers to one element per word", () => {
    const nodes = [
      span([], span([], text("i"))),
      span([], span([], text("f"))),
      span([], span(["space_char"], text(" "))),
      span([], span([], text(""))),
      span([], span([], text("x"))),
    ];

    expect(mergeAdjacentNodes(nodes)).toEqual([
      span([], span([], text("if")), span(["space_char"], text(" ")), span([], text("x"))),
    ]);
  });

  it("should not modify the given nodes", () => {
    const nodes = [span(["token"], text("a")), span(["token"], text("b"))];
    const copy = structuredClone(nodes);

    mergeAdjacentNodes(nodes);

    expect(nodes).toEqual(copy);
  });
});

describe("mergeAdjacentTokens", () => {
  it("should merge each line on its own", () => {
    const tokens = {
      old: [[text("a"), text("b")], [text("c")]],
      new: [[span(["token"], text("d")), span(["token"], text("e"))]],
    };

    expect(mergeAdjacentTokens(tokens)).toEqual({
      old: [[text("ab")], [text("c")]],
      new: [[span(["token"], text("de"))]],
    });
  });

  it("should restore the token trees of nested refractor tokens", () => {
    refractor.register(java);
    const hunks = [
      {
        content: "@@ -1,1 +1,1 @@",
        oldStart: 1,
        oldLines: 1,
        newStart: 1,
        newLines: 1,
        changes: [
          {
            type: "normal",
            isNormal: true,
            oldLineNumber: 1,
            newLineNumber: 1,
            content: "import com.cloudogu.BasicComment;",
          },
        ],
      },
    ] as HunkData[];

    // like the worker, react-diff-view needs the children of the highlighted root
    const highlight = (value: string, language: string) => refractor.highlight(value, language).children;

    const tokens = mergeAdjacentTokens(
      tokenize(hunks, { highlight: true, language: "java", refractor: { ...refractor, highlight } }),
    );

    const [line] = tokens.new;
    const importNode = line.find((node) => node.properties?.className?.includes("import"));
    expect(importNode?.children?.map((child) => child.properties?.className)).toEqual([
      ["token", "namespace"],
      ["token", "class-name"],
    ]);
  });
});
