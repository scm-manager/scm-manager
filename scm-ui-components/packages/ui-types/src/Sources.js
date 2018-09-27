// @flow

import type { Collection, Links } from "./hal";

// TODO ?? check ?? links
export type SubRepository = {
  repositoryUrl: string,
  browserUrl: string,
  revision: string
};

export type File = {
  name: string,
  path: string,
  directory: boolean,
  description?: string,
  length: number,
  lastModified?: string,
  subRepository?: SubRepository, // TODO
  _links: Links
};

export type SourcesCollection = Collection & {
  _embedded: {
    files: File[]
  }
};
