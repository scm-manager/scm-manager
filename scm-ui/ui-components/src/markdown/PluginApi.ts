import { Node, Parent } from "unist";

export type Visitor = (node: Node, index: number, parent?: Parent) => void;

export type AstPluginContext = {
  visit: (type: string, visitor: Visitor) => void;
};

export type AstPlugin = (context: AstPluginContext) => void;
