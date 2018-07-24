//@flow
import type { Link, Links } from "../../types/hal";

export type User = {
  displayName: string,
  name: string,
  mail: string,
  password: string,
  admin: boolean,
  active: boolean,
  _links: Links
};
