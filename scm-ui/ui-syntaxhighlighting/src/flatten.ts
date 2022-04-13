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
