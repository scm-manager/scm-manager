// @flow
import type { Link } from "@scm-manager/ui-types";

export type InfoItem = {
  title: string,
  summary: string,
  _links: {[string]: Link}
};
