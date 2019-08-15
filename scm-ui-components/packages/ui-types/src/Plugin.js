//@flow
import type {Collection, Links} from "./hal";


export type Plugin = {
  name: string,
  version: string,
  displayName: string,
  description?: string,
  author: string,
  category: string,
  avatarUrl: string,
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
