import { AstPlugin } from "./PluginApi";
import {MarkdownAbstractSyntaxTree, MdastPlugin} from "react-markdown";
// @ts-ignore No types available
import visit from "unist-util-visit";

export default function createMdastPlugin(plugin: AstPlugin): MdastPlugin {
  return (tree: MarkdownAbstractSyntaxTree) => {
    plugin({
      visit: (type, visitor) => visit(tree, type, visitor)
    });
    return tree;
  };
}
