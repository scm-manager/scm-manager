// @flow
import React from "react";
import { translate } from "react-i18next";
import UserRow from "./UserRow";
import type { User } from "../../types/User";

type Props = {
  t: string => string,
  users: User[]
};

class UserTable extends React.Component<Props> {
  render() {
    const { users, t } = this.props;
    return (
      <table className="table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th className="is-hidden-mobile">{t("user.name")}</th>
            <th>{t("user.displayName")}</th>
            <th>{t("user.mail")}</th>
            <th className="is-hidden-mobile">{t("user.admin")}</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user, index) => {
            return <UserRow key={index} user={user} />;
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("users")(UserTable);
