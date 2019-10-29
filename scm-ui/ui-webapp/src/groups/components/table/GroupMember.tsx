import React from "react";
import { Link } from "react-router-dom";
import styled from "styled-components";
import { Member } from "@scm-manager/ui-types";
import {Icon} from "@scm-manager/ui-components";

type Props = {
  member: Member;
};

const StyledMember = styled.li`
  display: inline-block;
  margin-right: 0.25rem;
  padding: 0.25rem 0.75rem;
  border: 1px solid #eee;
  border-radius: 4px;
`;

export default class GroupMember extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}><Icon name="user" color="inherit" /> {label}</Link>;
  }

  showName(to: any, member: Member) {
    if (member._links.self) {
      return this.renderLink(to, member.name);
    } else {
      return member.name;
    }
  }

  render() {
    const { member } = this.props;
    const to = `/user/${member.name}`;
    return <StyledMember>{this.showName(to, member)}</StyledMember>;
  }
}
