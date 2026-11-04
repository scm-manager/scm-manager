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

import React, { ChangeEvent, FC } from "react";
import classNames from "classnames";
import { Select, SelectField } from "@scm-manager/ui-core";

type Props = {
  availableRoles?: string[];
  handleRoleChange: (p: string) => void;
  role: string;
  label?: string;
  helpText?: string;
  loading?: boolean;
  emptyLabel?: string;
  "aria-label"?: string;
};

const createSelectOptions = (roles: string[]) => {
  return roles.map(role => {
    return {
      label: role,
      value: role
    };
  });
};

const RoleSelector: FC<Props> = ({
  availableRoles,
  role,
  handleRoleChange,
  loading,
  label,
  helpText,
  emptyLabel,
  "aria-label": ariaLabel,
}) => {
  if (!availableRoles) {
    return null;
  }
  const options = role
    ? createSelectOptions(availableRoles)
    : [{ label: emptyLabel || "", value: "" }, ...createSelectOptions(availableRoles)];

  const selectProps = {
    onChange: (event: ChangeEvent<HTMLSelectElement>) => handleRoleChange(event.target.value),
    value: role ? role : "",
    options,
  };

  if (label) {
    return <SelectField label={label} helpText={helpText} {...selectProps} />;
  }
  return <Select className={classNames({ "is-loading": loading })} aria-label={ariaLabel} {...selectProps} />;
};

export default RoleSelector;
