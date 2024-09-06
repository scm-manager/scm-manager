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

import React from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Loading } from "@scm-manager/ui-components";
import PermissionCheckbox from "./PermissionCheckbox";

type Props = {
  permissions: {
    [key: string]: boolean;
  };
  onChange: (value: boolean, name: string) => void;
  disabled: boolean;
  role?: boolean;
};

const StyledWrapper = styled.div`
  & .field .control {
    width: 100%;
    word-wrap: break-word;
  }
`;

export default class PermissionsWrapper extends React.Component<Props> {
  render() {
    const { permissions, onChange, disabled, role } = this.props;

    if (!permissions) {
      return <Loading />;
    }

    const permissionArray = Object.keys(permissions);
    return (
      <div className="columns">
        <StyledWrapper className={classNames("column", "is-half", "pb-0")}>
          {permissionArray.slice(0, permissionArray.length / 2 + 1).map(p => (
            <PermissionCheckbox
              key={p}
              name={p}
              checked={permissions[p]}
              onChange={onChange}
              disabled={disabled}
              role={role}
            />
          ))}
        </StyledWrapper>
        <StyledWrapper className={classNames("column", "is-half")}>
          {permissionArray.slice(permissionArray.length / 2 + 1, permissionArray.length).map(p => (
            <PermissionCheckbox
              key={p}
              name={p}
              checked={permissions[p]}
              onChange={onChange}
              disabled={disabled}
              role={role}
            />
          ))}
        </StyledWrapper>
      </div>
    );
  }
}
