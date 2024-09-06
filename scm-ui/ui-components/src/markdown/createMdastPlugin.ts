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
        visit: (type, visitor) => visit(tree, type, visitor),
      });
      return tree;
    };
  };
}
