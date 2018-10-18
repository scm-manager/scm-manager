//@flow
import type { Links } from "./hal";

export type Tag = {
  name: string,
  revision: string,
  _links: Links
}
