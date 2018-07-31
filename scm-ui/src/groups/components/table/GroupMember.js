// @flow
import React from "react";
import { Link } from "react-router-dom";
import type {User} from "../../../users/types/User";

type Props = {
  member: User
};

export default class GroupMember extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  showName(to: any, member:User) {
    if(member._links.self){
      return  this.renderLink(to, member.name);
    }
    else {
      return member.name
    }
  }

  render() {
    const { member } = this.props;
    const to = `/user/${member.name}`;
    return (
      <tr className="is-hidden-mobile">
        <td>
          {this.showName(to, member)}
        </td>
      </tr>

    );
  }
}
