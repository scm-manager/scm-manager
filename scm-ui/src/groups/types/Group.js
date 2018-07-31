//@flow
import type { Links } from "../../types/hal";
import type { User } from "../../users/types/User";

export type Group = {
  name: string,
  creationDate: string,
  description: string,
  lastModified: string,
  type: string,
  properties: [],
  members: string[],
  _links: Links,
  _embedded: {
    members: User[]
  }
};
