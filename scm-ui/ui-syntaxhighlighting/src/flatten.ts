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
import type { Element } from "hast";
import { isRefractorElement, RefractorNode } from "./types";

function flatten(
  nodes: RefractorNode[],
  nodeLimit = Number.MAX_SAFE_INTEGER,
  className: string[] = [],
  totalNodes = nodes.length
): FlatNodes {
  const result: FlatNodes = [];
  for (const node of nodes) {
    if (isRefractorElement(node)) {
      const subNodes = flatten(
        node.children,
        nodeLimit,
        [...className, ...(((node as Element).properties?.className as string[]) ?? [])],
        totalNodes
      );
      totalNodes += subNodes.length;
      result.push(...subNodes);
    } else if (className.length) {
      result.push({
        type: "element",
        tagName: "span",
        properties: { className: Array.from(new Set(className)) },
        children: [node],
      });
    } else {
      result.push(node);
    }
  }
  if (totalNodes > nodeLimit) {
    throw new Error(`Node limit (${nodeLimit}) reached. Current nodes ${totalNodes}`);
  }
  return result;
}

export default flatten;

export type FlatNodes = FlatNode[];
export type FlatNode = FlatElement | FlatText;
export type FlatElement = {
  type: "element";
  tagName: "span";
  properties: { className: string[] };
  children: [FlatText];
};
export type FlatText = {
  type: "text";
  value: string;
};
