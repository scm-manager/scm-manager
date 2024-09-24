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

import { nameRegex } from "../validation";
import { TFunction } from "i18next";
import { AstPlugin } from "./PluginApi";
import { Literal, Node, Parent } from "unist";

const namePartRegex = nameRegex.source.substring(1, nameRegex.source.length - 1).replace(/\[\^([^\]s]+)\]/, "[^$1\\s]");

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

export const createTransformer = (t: TFunction): AstPlugin => {
  return ({ visit }) => {
    visit("text", (node: Node, index: number, parent?: Parent) => {
      if (!parent || parent.type === "link" || !(node as Literal).value) {
        return;
      }

      let nodeText = (node as Literal).value as string;
      const matches = match(nodeText);

      if (matches.length > 0) {
        const children = [];
        for (const m of matches) {
          const i = nodeText.indexOf(m[0]);
          if (i > 0) {
            children.push({
              type: "text",
              value: nodeText.substring(0, i),
            });
          }

          children.push({
            type: "link",
            url: `/repo/${m[1]}/${m[2]}/code/changeset/${m[3]}`,
            title: t("changeset.shortlink.title", {
              namespace: m[1],
              name: m[2],
              id: m[3],
            }),
            children: [
              {
                type: "text",
                value: m[0],
              },
            ],
          });

          nodeText = nodeText.substring(i + m[0].length);
        }

        if (nodeText.length > 0) {
          children.push({
            type: "text",
            value: nodeText,
          });
        }

        parent.children[index] = {
          type: "text",
          children,
        } as Node;
      }
    });
  };
};
