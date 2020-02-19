import { Links } from "./hal";

export type Me = {
  name: string;
  displayName: string;
  mail: string;
  groups: string[];
  _links: Links;
};
