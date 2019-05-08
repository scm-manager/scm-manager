//@flow

import type { Links } from "./hal";

export type Role = {
  name: string,
  verbs: string[],
  creationDate?: number,
  lastModified?: number,
  system: boolean,
  _links: Links
};
