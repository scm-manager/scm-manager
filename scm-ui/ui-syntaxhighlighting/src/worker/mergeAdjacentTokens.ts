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

import type { HunkTokens, TokenNode } from "react-diff-view";

const isEqual = (a: unknown, b: unknown): boolean => {
  if (a === b) {
    return true;
  }
  if (typeof a !== "object" || typeof b !== "object" || a === null || b === null) {
    return false;
  }
  if (Array.isArray(a) !== Array.isArray(b)) {
    return false;
  }
  const x = a as Record<string, unknown>;
  const y = b as Record<string, unknown>;
  const keys = Object.keys(x);
  return keys.length === Object.keys(y).length && keys.every((key) => isEqual(x[key], y[key]));
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const withoutChildren = ({ children, ...node }: TokenNode) => node;

const isMergeable = (previous: TokenNode, node: TokenNode) => {
  if (previous.type !== node.type) {
    return false;
  }
  if (previous.type === "text") {
    return true;
  }
  return !!previous.children && !!node.children && isEqual(withoutChildren(previous), withoutChildren(node));
};

const merge = (previous: TokenNode, node: TokenNode): TokenNode => {
  if (previous.type === "text") {
    return { ...previous, value: `${previous.value}${node.value}` };
  }
  return { ...previous, children: [...(previous.children ?? []), ...(node.children ?? [])] };
};

export const mergeAdjacentNodes = (nodes: TokenNode[]): TokenNode[] => {
  const merged: TokenNode[] = [];
  for (const node of nodes) {
    const previous = merged[merged.length - 1];
    if (previous && isMergeable(previous, node)) {
      merged[merged.length - 1] = merge(previous, node);
    } else {
      merged.push(node);
    }
  }
  return merged.map((node) => (node.children ? { ...node, children: mergeAdjacentNodes(node.children) } : node));
};

/**
 * Since version 3, react-diff-view no longer merges adjacent nodes with equal properties when it rebuilds the
 * token trees, so every split of a text node (line breaks, whitespace markers, nested tokens) ends up in its own
 * element. This restores the trees version 2 produced: adjacent text nodes are concatenated and adjacent elements
 * with equal properties are combined into one element holding the children of both.
 */
const mergeAdjacentTokens = (tokens: HunkTokens): HunkTokens => ({
  old: tokens.old.map(mergeAdjacentNodes),
  new: tokens.new.map(mergeAdjacentNodes),
});

export default mergeAdjacentTokens;
