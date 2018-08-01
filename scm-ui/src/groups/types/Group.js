//@flow
import type { Links } from "../../types/hal";
import type { User } from "../../users/types/User";

export type Group = {
  name: string,
  description: string,
  type: string,
  members: string[],
  _links: Links,
  _embedded: {
    members: User[]
  }
};
