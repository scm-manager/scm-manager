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

import { Collection, Links, PagedCollection } from "./hal";

export type Member = {
  name: string;
  _links: Links;
};

export type GroupBase = {
  name: string;
  description: string;
  type: string;
  external: boolean;
  members: string[];
};

export type Group = Collection &
  GroupBase & {
    creationDate?: string;
    lastModified?: string;
    _embedded: {
      members: Member[];
    };
  };

export type GroupCreation = GroupBase;

export type GroupCollection = PagedCollection & {
  _embedded: {
    groups: Group[];
  };
};
