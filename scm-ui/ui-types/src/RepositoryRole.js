// @flow

import type {Links} from "./hal";

export type RepositoryRole = {
  name: string,
  verbs: string[],
  type?: string,
  creationDate?: string,
  lastModified?: string,
  _links: Links
};

