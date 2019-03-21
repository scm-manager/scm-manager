// @flow
import React from "react";
import { Link } from "react-router-dom";
import type { Group } from "@scm-manager/ui-types";
import { Checkbox } from "@scm-manager/ui-components"

type Props = {
  group: Group
};

export default class GroupRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { group } = this.props;
    const to = `/group/${group.name}`;
    return (
      <tr>
        <td>{this.renderLink(to, group.name)}</td>
        <td className="is-hidden-mobile">{group.description}</td>
        <td>
          <Checkbox checked={group.external} />
        </td>
      </tr>
    );
  }
}
