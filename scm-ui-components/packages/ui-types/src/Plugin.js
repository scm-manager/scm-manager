//@flow
import type { PagedCollection, Links } from "./hal";

export type Plugin = {
  namespace: string,
  name: string,
  type: string,
  description?: string,
  creationDate?: string,
  lastModified?: string,
  _links: Links
};

export type PluginCollection = PagedCollection & {
  _embedded: {
    plugins: Plugin[] | string[]
  }
};

export type PluginGroup = {
  name: string,
  plugins: Plugin[]
};
