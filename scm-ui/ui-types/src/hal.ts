export type Link = {
  href: string;
  name?: string;
};

type LinkValue = Link | Link[];

export type Links = {
  [key: string]: LinkValue;
};

export type Collection = {
  _embedded?: any;
  _links: Links;
};

export type PagedCollection = Collection & {
  page: number;
  pageTotal: number;
};
