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

import { AstPlugin } from "./PluginApi";
import { Literal, Node, Parent } from "unist";

/**
 * Some existing remark plugins (e.g. changesetShortLinkParser or the plugin for issue tracker links) create
 * text nodes without values but with children. This does not get parsed properly by remark2rehype.
 * This remark-plugin takes the children of these invalid text nodes and inserts them into the node's parent
 * in place of the text node.
 *
 * @example
 * ```
 * # This ->
 *
 * validNode
 * invalidNode
 *  child1
 *  child2
 *  child3
 * validNode
 *
 * # Becomes ->
 *
 * validNode
 * child1
 * child2
 * child3
 * validNode
 * ```
 */
export const createTransformer = (): AstPlugin => {
  return ({ visit }) => {
    visit("text", (node: Node, index: number, parent?: Parent) => {
      if ((node as Literal).value === undefined && Array.isArray((node as Parent).children) && (node as Parent).children.length > 0) {
        const children = (node as Parent).children;
        const preChildren = parent?.children.slice(0, index) || [];
        const postChildren = parent?.children.slice(index + 1) || [];
        parent!.children = [...preChildren, ...children, ...postChildren];
      }
    });
  };
};
