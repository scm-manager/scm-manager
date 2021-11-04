/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
          {permissionArray.slice(0, permissionArray.length / 2 + 1).map((p) => (
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
          {permissionArray.slice(permissionArray.length / 2 + 1, permissionArray.length).map((p) => (
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
