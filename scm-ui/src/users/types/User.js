//@flow
import type { Link, Links } from "../../types/hal";

export type User = {
  displayName: string,
  name: string,
  mail: string,
  admin: boolean,
  _links: Links
};
