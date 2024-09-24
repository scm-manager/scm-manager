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

export type Link = {
  href: string;
  name?: string;
  templated?: boolean;
};

type LinkValue = Link | Link[];

export type Links = {
  [key: string]: LinkValue;
};

export type Embedded = {
  [key: string]: unknown;
};

export type HalRepresentation = {
  _embedded?: Embedded;
  _links: Links;
};

export type HalRepresentationWithEmbedded<T extends Embedded> = HalRepresentation & {
  _embedded?: T;
};

export type PagedCollection<T extends Embedded = Embedded> = HalRepresentationWithEmbedded<T> & {
  page: number;
  pageTotal: number;
};

/**
 * @deprecated use HalRepresentation instead
 */
export type Collection = HalRepresentation;

