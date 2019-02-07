// @flow
import type { Changeset, Repository } from "@scm-manager/ui-types";

export type Description = {
  title: string,
  message: string
};

export function createChangesetLink(repository: Repository, changeset: Changeset) {
  return `/repo/${repository.namespace}/${repository.name}/changeset/${changeset.id}`;
}

export function createSourcesLink(repository: Repository, changeset: Changeset) {
  return `/repo/${repository.namespace}/${repository.name}/sources/${changeset.id}`;
}

export function parseDescription(description?: string): Description {
  const desc = description ? description : "";
  const lineBreak = desc.indexOf("\n");

  let title;
  let message = "";

  if (lineBreak > 0) {
    title = desc.substring(0, lineBreak);
    message = desc.substring(lineBreak + 1);
  } else {
    title = desc;
  }

  return {
    title,
    message
  };
}
