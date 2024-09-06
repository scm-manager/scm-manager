/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { validation } from "@scm-manager/ui-components";
import { Permission } from "@scm-manager/ui-types";

const isNameValid = validation.isNameValid;

export { isNameValid };

export const isPermissionValid = (name: string, groupPermission: boolean, permissions: Permission[]) => {
  return isNameValid(name) && !currentPermissionIncludeName(name, groupPermission, permissions);
};

const currentPermissionIncludeName = (name: string, groupPermission: boolean, permissions: Permission[]) => {
  for (let i = 0; i < permissions.length; i++) {
    if (permissions[i].name === name && permissions[i].groupPermission === groupPermission) {
      return true;
    }
  }
  return false;
};
