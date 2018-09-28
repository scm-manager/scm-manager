// @flow
export type Link = {
  href: string,
  name?: string
};

export type Links = { [string]: Link | Link[] };

export type Collection = {
  _embedded: Object,
  _links: Links
};

export type PagedCollection = Collection & {
  page: number,
  pageTotal: number
};
