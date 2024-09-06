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
