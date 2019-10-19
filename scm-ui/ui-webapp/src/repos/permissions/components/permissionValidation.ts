import { validation } from '@scm-manager/ui-components';
import { PermissionCollection } from '@scm-manager/ui-types';

const isNameValid = validation.isNameValid;

export { isNameValid };

export const isPermissionValid = (
  name: string,
  groupPermission: boolean,
  permissions: PermissionCollection,
) => {
  return (
    isNameValid(name) &&
    !currentPermissionIncludeName(name, groupPermission, permissions)
  );
};

const currentPermissionIncludeName = (
  name: string,
  groupPermission: boolean,
  permissions: PermissionCollection,
) => {
  for (let i = 0; i < permissions.length; i++) {
    if (
      permissions[i].name === name &&
      permissions[i].groupPermission === groupPermission
    )
      return true;
  }
  return false;
};
