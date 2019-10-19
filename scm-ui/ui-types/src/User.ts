import { Links } from './hal';

export type DisplayedUser = {
  id: string;
  displayName: string;
  mail: string;
};

export type User = {
  displayName: string;
  name: string;
  mail: string;
  password: string;
  active: boolean;
  type?: string;
  creationDate?: string;
  lastModified?: string;
  _links: Links;
};
