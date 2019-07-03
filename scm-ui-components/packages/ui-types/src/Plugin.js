//@flow
import type { Collection, Links } from "./hal";

export type Plugin = {
  name: string,
  type: string,
  description?: string,
  creationDate?: string,
  lastModified?: string,
  _links: Links
};

export type PluginCollection = Collection & {
  _embedded: {
    plugins: Plugin[] | string[]
  }
};

export type PluginGroup = {
  name: string,
  plugins: Plugin[]
};
