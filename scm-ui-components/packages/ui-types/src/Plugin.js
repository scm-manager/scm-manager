//@flow
import type { Collection, Links } from "./hal";

export type Plugin = {
  name: string,
  type: string,
  version: string,
  author: string,
  description?: string,
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
