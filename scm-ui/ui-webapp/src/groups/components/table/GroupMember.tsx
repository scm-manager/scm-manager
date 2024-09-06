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
import { Link } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import { Member } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";

type Props = {
  member: Member;
};

const StyledMember = styled.li`
  border: 1px solid #eee;
  border-radius: 4px;
`;

export default class GroupMember extends React.Component<Props> {
  showName(to: string, member: Member) {
    const userComponent = (
      <>
        <Icon name="user" color="inherit" alt="" /> {member.name}
      </>
    );
    if (member._links?.self) {
      return <Link to={to}>{userComponent}</Link>;
    } else {
      return userComponent;
    }
  }

  render() {
    const { member } = this.props;
    const to = `/user/${member.name}`;
    return (
      <StyledMember className={classNames("is-inline-block", "mr-1", "px-3", "py-1")}>
        {this.showName(to, member)}
      </StyledMember>
    );
  }
}
