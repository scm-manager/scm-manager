//@flow
import type { Collection } from "../../types/Collection";
import type { Links } from "../../types/hal";

export type Member = {
  name: string,
  _links: Links
};

export type Group = Collection & {
  name: string,
  description: string,
  type: string,
  members: string[],
  _embedded: {
    members: Member[]
  }
};
