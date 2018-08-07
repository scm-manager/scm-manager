//@flow
import type { Links } from "../../types/hal";
import type { PagedCollection } from "../../types/Collection";

export type Repository = {
  namespace: string,
  name: string,
  type: string,
  contact?: string,
  description?: string,
  creationDate?: string,
  lastModified?: string,
  _links: Links
};

export type RepositoryCollection = PagedCollection & {
  _embedded: {
    repositories: Repository[] | string[]
  }
};

export type RepositoryGroup = {
  name: string,
  repositories: Repository[]
};
