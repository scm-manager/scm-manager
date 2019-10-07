// @flow
export type Link = {
  href: string,
  name?: string
};

type LinkValue = Link | Link[];

// TODO use LinkValue
export type Links = { [string]: any };

export type Collection = {
  _embedded: Object,
  // $FlowFixMe
  _links: Links
};

export type PagedCollection = Collection & {
  page: number,
  pageTotal: number
};
