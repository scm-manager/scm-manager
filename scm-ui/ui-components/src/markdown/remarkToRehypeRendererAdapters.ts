import React from "react";

export const createRemark2RehypeCodeRendererAdapter = (remarkRenderer: any) => {
  return ({ node, children }: any) => {
    children = children || [];
    const renderProps = {
      value: children[0],
      language: Array.isArray(node.properties.className) ? node.properties.className[0].split("language-")[1] : ""
    };
    return React.createElement(remarkRenderer, renderProps, ...children);
  };
};

export const createRemark2RehypeLinkRendererAdapter = (remarkRenderer: any) => {
  return ({ node, children }: any) => {
    const renderProps = {
      href: node.properties.href || ""
    };
    children = children || [];
    return React.createElement(remarkRenderer, renderProps, ...children);
  };
};

export const createRemark2RehypeHeadingRendererAdapterFactory = (remarkRenderer: any, permalink?: string) => {
  return (level: number) => ({ node, children }: any) => {
    const renderProps = {
      id: node.properties.id,
      level,
      permalink
    };
    children = children || [];
    return React.createElement(remarkRenderer, renderProps, ...children);
  };
};
