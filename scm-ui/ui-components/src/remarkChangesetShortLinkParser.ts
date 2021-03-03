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

import { nameRegex } from "./validation";
// @ts-ignore No types available
import visit from "unist-util-visit";
import { TFunction } from "i18next";

const namePartRegex = nameRegex.source.substring(1, nameRegex.source.length - 1);

export const regExpPattern = `(${namePartRegex})\\/(${namePartRegex})@([\\w\\d]+)`;

function match(value: string): RegExpMatchArray[] {
  const regExp = new RegExp(regExpPattern, "g");
  const matches = [];
  let m = regExp.exec(value);
  while (m) {
    matches.push(m);
    m = regExp.exec(value);
  }
  return matches;
}

export const createTransformer = (t: TFunction) => {
  return (tree: any) => {
    visit(tree, "text", (node: any, index: number, parent: any) => {
      if (parent.type === "link" || !node.value) {
        return;
      }

      let nodeText = node.value;
      const matches = match(nodeText);

      if (matches.length > 0) {
        const children = [];
        for (const m of matches) {
          const i = nodeText.indexOf(m[0]);
          if (i > 0) {
            children.push({
              type: "text",
              value: nodeText.substring(0, i)
            });
          }

          children.push({
            type: "link",
            url: `/repo/${m[1]}/${m[2]}/code/changeset/${m[3]}`,
            title: t("changeset.shortlink.title", {
              namespace: m[1],
              name: m[2],
              id: m[3]
            }),
            children: [
              {
                type: "text",
                value: m[0]
              }
            ]
          });

          nodeText = nodeText.substring(i + m[0].length);
        }

        if (nodeText.length > 0) {
          children.push({
            type: "text",
            value: nodeText
          });
        }

        parent.children![index] = {
          type: "text",
          children
        };
      }
    });
    return tree;
  };
};
