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

import { HalRepresentation, Link } from "@scm-manager/ui-types";
import { MissingLinkError } from "./errors";

export const requiredLink = (object: HalRepresentation, name: string, subName?: string): string => {
  const link = object._links[name];
  if (!link) {
    throw new MissingLinkError(`could not find link with name ${name}`);
  }
  if (Array.isArray(link)) {
    if (subName) {
      const subLink = link.find((l: Link) => l.name === subName);
      if (subLink) {
        return subLink.href;
      }
      throw new Error(`could not return href, sub-link ${subName} in ${name} does not exist`);
    }
    throw new Error(`could not return href, link ${name} is a multi link`);
  }
  return link.href;
};

export const objectLink = (object: HalRepresentation, name: string) => {
  const link = object._links[name];
  if (!link) {
    return null;
  }
  if (Array.isArray(link)) {
    throw new Error(`could not return href, link ${name} is a multi link`);
  }
  return link.href;
};
