import { Collection, Links } from './hal';

export type Plugin = {
  name: string;
  version: string;
  newVersion?: string;
  displayName: string;
  description?: string;
  author: string;
  category: string;
  avatarUrl: string;
  pending: boolean;
  markedForUninstall?: boolean;
  dependencies: string[];
  _links: Links;
};

export type PluginCollection = Collection & {
  _links: Links;
  _embedded: {
    plugins: Plugin[] | string[];
  };
};

export type PluginGroup = {
  name: string;
  plugins: Plugin[];
};

export type PendingPlugins = {
  _links: Links;
  _embedded: {
    new: [];
    update: [];
    uninstall: [];
  };
};
