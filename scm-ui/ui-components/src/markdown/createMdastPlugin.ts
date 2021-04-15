import { AstPlugin } from "./PluginApi";
// @ts-ignore No types available
import visit from "unist-util-visit";

export default function createMdastPlugin(plugin: AstPlugin): any {
  return function attach() {
    return function transform(tree: any) {
      plugin({
        visit: (type, visitor) => visit(tree, type, visitor)
      });
      return tree;
    };
  };
}
