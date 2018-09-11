//@flow
import type { Links } from "./hal";
import type { Tag } from "./Tags";
export type Changeset = {
  id: string,
  date: Date,
  author: {
    name: string,
    mail: string
  },
  description: string
  _links: Links,
  _embedded: {
    tags: Tag[]
    branches: any, //todo: Add correct type
    parents: any //todo: Add correct type
  };
}
