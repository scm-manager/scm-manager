import { Links } from './hal';

export type Me = {
  name: string;
  displayName: string;
  mail: string;
  groups: [];
  _links: Links;
};
