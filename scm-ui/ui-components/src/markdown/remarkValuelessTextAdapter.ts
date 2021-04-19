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

import { AstPlugin } from "./PluginApi";
import { Node, Parent } from "unist";

/**
 * Some existing remark plugins (e.g. changesetShortLinkParser or the plugin for issue tracker links) create
 * text nodes without values but with children. This does not get parsed properly by remark2rehype.
 * This remark-plugin transforms all of these invalid text nodes to valid paragraphs.
 */
export const createTransformer = (): AstPlugin => {
  return ({ visit }) => {
    visit("text", (node: Node, index: number, parent?: Parent) => {
      if (node.value === undefined && Array.isArray(node.children) && node.children.length > 0) {
        node.type = "paragraph";
      }
    });
  };
};
