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

/*eslint-disable */
import flatten, { FlatElement, FlatNode, FlatNodes, FlatText } from "./flatten";
import type { RefractorNode } from "../types";

function groupByLines(nodes: RefractorNode[], nodeLimit?: number): Array<LineElement> {
  return group(flatten(nodes, nodeLimit));
}

export default groupByLines;

export type LineElement = {
  type: "element";
  tagName: "span";
  properties: {
    className: ["line"];
    ["data-line-number"]: number;
  };
  children: FlatNodes;
};
export { FlatNodes, FlatNode, FlatElement, FlatText };

function group(nodes: FlatNodes): Array<LineElement> {
  const lineElements: Array<LineElement> = [];
  let currentLine = createLineElement(1);
  for (const node of nodes) {
    const lines = splitByLines(node);
    const last = lines.length - 1;
    lines.forEach((line, index) => {
      if (line.type !== "text" || line.value !== "") {
        currentLine.children.push(line);
      }
      if (index !== last) {
        lineElements.push(currentLine);
        currentLine = createLineElement(lineElements.length + 1);
      }
    });
  }
  if (currentLine.children.length > 0) {
    lineElements.push(currentLine);
  }
  return lineElements;
}

function createLineElement(lineNumber: number): LineElement {
  return {
    type: "element",
    tagName: "span",
    properties: {
      className: ["line"],
      ["data-line-number"]: lineNumber,
    },
    children: [],
  };
}

function splitByLines(node: FlatNode): FlatNodes {
  if (node.type === "text") {
    return splitTextByLines(node);
  } else {
    const texts = splitTextByLines(node.children[0]);
    return texts.map<FlatElement>((text) => ({ ...node, children: [text] }));
  }
}

function splitTextByLines(text: FlatText): Array<FlatText> {
  if (text.value.length === 0) {
    return [text];
  }
  const values = text.value.split("\n");
  const last = values.length - 1;
  return values.map<FlatText>((value, index) => ({
    type: "text",
    value: value + (index === last ? "" : "\n"),
  }));
}
