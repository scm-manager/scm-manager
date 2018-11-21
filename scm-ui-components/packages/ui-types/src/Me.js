// @flow

import type { Links } from "./hal";

export type Me = {
  name: string,
  displayName: string,
  mail: string,
  _links: Links
};
