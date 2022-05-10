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
