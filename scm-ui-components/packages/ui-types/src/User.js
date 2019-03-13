//@flow
import type { Links } from "./hal";

export type User = {
  displayName: string,
  name: string,
  mail: string,
  password: string,
  active: boolean,
  type?: string,
  creationDate?: string,
  lastModified?: string,
  _links: Links
};
