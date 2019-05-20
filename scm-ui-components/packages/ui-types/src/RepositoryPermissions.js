//@flow
import type {Links} from "./hal";

export type PermissionCreateEntry = {
  name: string,
  role?: string,
  verbs?: string[],
  groupPermission: boolean
}

export type Permission = PermissionCreateEntry & {
  _links: Links
};

export type PermissionCollection = Permission[];
