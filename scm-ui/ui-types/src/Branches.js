//@flow
import type {Links} from "./hal";

export type Branch = {
  name: string,
  revision: string,
  defaultBranch?: boolean,
  _links: Links
}

export type BranchRequest = {
  name: string,
  parent: string
}
