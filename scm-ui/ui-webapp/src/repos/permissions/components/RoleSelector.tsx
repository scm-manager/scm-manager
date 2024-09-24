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

import React, { FC } from "react";
import { Select } from "@scm-manager/ui-components";

type Props = {
  availableRoles?: string[];
  handleRoleChange: (p: string) => void;
  role: string;
  label?: string;
  helpText?: string;
  loading?: boolean;
  emptyLabel?: string;
};

const createSelectOptions = (roles: string[]) => {
  return roles.map(role => {
    return {
      label: role,
      value: role
    };
  });
};

const RoleSelector: FC<Props> = ({ availableRoles, role, handleRoleChange, loading, label, helpText, emptyLabel }) => {
  if (!availableRoles) {
    return null;
  }
  const options = role
    ? createSelectOptions(availableRoles)
    : [{ label: emptyLabel || "", value: "" }, ...createSelectOptions(availableRoles)];

  return (
    <Select
      onChange={handleRoleChange}
      value={role ? role : ""}
      options={options}
      loading={loading}
      label={label}
      helpText={helpText}
    />
  );
};

export default RoleSelector;
