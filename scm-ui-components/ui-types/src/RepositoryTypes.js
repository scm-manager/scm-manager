// @flow

import type { Collection } from "./hal";

export type RepositoryType = {
  name: string,
  displayName: string
};

export type RepositoryTypeCollection = Collection & {
  _embedded: {
    repositoryTypes: RepositoryType[]
  }
};
