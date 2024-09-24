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
