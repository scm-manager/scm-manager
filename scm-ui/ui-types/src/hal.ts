export type Link = {
  href: string;
  name?: string;
};

type LinkValue = Link | Link[];

// TODO use LinkValue
export type Links = {
  [key: string]: any;
};

export type Collection = {
  _embedded: object;
  // $FlowFixMe
  _links: Links;
};

export type PagedCollection = Collection & {
  page: number;
  pageTotal: number;
};
