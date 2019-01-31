// @flow

export type RepositoryRole = {
  name: string,
  verbs: string[]
};

export type AvailableRepositoryPermissions = {
  availableVerbs: string[],
  availableRoles: RepositoryRole[]
};
