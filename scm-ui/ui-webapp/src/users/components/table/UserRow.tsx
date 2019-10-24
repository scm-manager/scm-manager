import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { User } from "@scm-manager/ui-types";
import { Icon } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  user: User;
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
        <td>
          {iconType} {this.renderLink(to, user.name)}
        </td>
        <td className="is-hidden-mobile">{this.renderLink(to, user.displayName)}</td>
        <td>
          <a href={`mailto:${user.mail}`}>{user.mail}</a>
        </td>
      </tr>
    );
  }
}

export default withTranslation("users")(UserRow);
