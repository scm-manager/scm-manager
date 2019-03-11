// @flow

import type { Links } from "./hal";

export type NamespaceStrategies = {
  current: string,
  available: string[],
  _links: Links
};
