import { Links } from "./hal";

// TODO ?? check ?? links
export type SubRepository = {
  repositoryUrl: string;
  browserUrl: string;
  revision: string;
};

export type File = {
  name: string;
  path: string;
  directory: boolean;
  description?: string;
  revision: string;
  length?: number;
  commitDate?: string;
  subRepository?: SubRepository; // TODO
  partialResult: boolean;
  computationAborted: boolean;
  truncated: boolean;
  _links: Links;
  _embedded: {
    children: File[] | null | undefined;
  };
};
