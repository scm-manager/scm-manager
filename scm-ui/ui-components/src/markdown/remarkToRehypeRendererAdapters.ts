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
import React from "react";

export const createRemark2RehypeCodeRendererAdapter = (remarkRenderer: any) => {
  const codeBlock = ({ node, children }: any) => {
    children = children || [];
    const renderProps = {
      value: children[0],
      language: Array.isArray(node.properties.className) ? node.properties.className[0].split("language-")[1] : "",
    };
    return React.createElement(remarkRenderer, renderProps, ...children);
  };

  return ({ node, children }: any) => {
    children = children || [];

    if (children.length === 1) {
      const code = node.children[0];
      if (code.tagName === "code") {
        return codeBlock({ node: code, children: children[0].props.children });
      }
    }
    return React.createElement(node.tagName, {}, ...children);
  };
};

export const createRemark2RehypeLinkRendererAdapter = (remarkRenderer: any) => {
  return ({ node, children }: any) => {
    children = children || [];
    return React.createElement(remarkRenderer, node.properties, ...children);
  };
};

export const createRemark2RehypeHeadingRendererAdapterFactory = (remarkRenderer: any, permalink?: string) => {
  return (level: number) =>
    ({ node, children }: any) => {
      const renderProps = {
        id: node.properties.id,
        level: Math.min(level + 1, 6),
        permalink,
      };
      children = children || [];
      return React.createElement(remarkRenderer, renderProps, ...children);
    };
};
