//@flow
import type { Links } from "../../types/hal";

export type User = {
  displayName: string,
  name: string,
  mail: string,
  password: string,
  admin: boolean,
  active: boolean,
  _links: Links
};
