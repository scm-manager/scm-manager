//@flow
import type { Collection, Links } from "./hal";

export type Member = {
  name: string,
  _links: Links
};

export type Group = Collection & {
  name: string,
  description: string,
  type: string,
  external: boolean,
  members: string[],
  _embedded: {
    members: Member[]
  },
  creationDate?: string,
  lastModified?: string
};
