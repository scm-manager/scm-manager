//@flow
import type { Links } from "@scm-manager/ui-types";

export type Changeset = {
  id: String,
  author: {
    mail: String,
    name: String
  },
  date: String,
  description: String,
  _links: Links
};
