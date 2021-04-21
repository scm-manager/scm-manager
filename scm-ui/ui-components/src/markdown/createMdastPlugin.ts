import { AstPlugin } from "./PluginApi";
// @ts-ignore No types available
import visit from "unist-util-visit";

/**
 * Transforms the abstraction layer into an actual remark plugin to be used with unified.
 *
 * @see https://unifiedjs.com/learn/guide/create-a-plugin/
 */
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
