import React from "react";
import classNames from "classnames";
import styled from "styled-components";
import PermissionCheckbox from "./PermissionCheckbox";
import { Loading } from "@scm-manager/ui-components";

type Props = {
  permissions: {
    [key: string]: boolean;
  };
  onChange: (value: boolean, name: string) => void;
  disabled: boolean;
  role?: boolean;
};

const StyledWrapper = styled.div`
  padding-bottom: 0;

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
        <StyledWrapper className={classNames("column", "is-half")}>
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
