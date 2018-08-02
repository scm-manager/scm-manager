// @flow

import type { Collection } from "../../types/Collection";

export type RepositoryType = {
  name: string,
  displayName: string
};

export type RepositoryTypeCollection = Collection & {
  _embedded: {
    "repository-types": RepositoryType[]
  }
};
