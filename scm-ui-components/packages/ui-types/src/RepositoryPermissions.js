//@flow
import type { Links } from "./hal";

export type Permission = PermissionCreateEntry & {
  _links: Links
};

export type PermissionCreateEntry = {
  name: string,
  type: string,
  groupPermission: boolean
}

export type PermissionCollection = Permission[];
