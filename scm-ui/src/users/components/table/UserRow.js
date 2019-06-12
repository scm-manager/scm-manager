// @flow
import React from "react";
import { translate } from "react-i18next";
import { Link } from "react-router-dom";
import type { User } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";

type Props = {
  user: User,

  // context props
  t: string => string
};

class UserRow extends React.Component<Props> {
  renderLink(to: string, label: string) {
    return <Link to={to}>{label}</Link>;
  }

  render() {
    const { user, t } = this.props;
    const to = `/user/${user.name}`;
    const iconType = user.active ? (
      <Icon title={t("user.active")} name="user" />
    ) : (
      <Icon title={t("user.inactive")} name="user-slash" />
    );

    return (
      <tr className={user.active ? "border-is-green" : "border-is-yellow"}>
        <td>{iconType} {this.renderLink(to, user.name)}</td>
        <td className="is-hidden-mobile">
          {this.renderLink(to, user.displayName)}
        </td>
        <td>
          <a href={`mailto:${user.mail}`}>{user.mail}</a>
        </td>
      </tr>
    );
  }
}

export default translate("users")(UserRow);
