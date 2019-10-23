import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { User } from "@scm-manager/ui-types";
import UserRow from "./UserRow";

type Props = WithTranslation & {
  users: User[];
};

class UserTable extends React.Component<Props> {
  render() {
    const { users, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th className="is-hidden-mobile">{t("user.name")}</th>
            <th>{t("user.displayName")}</th>
            <th>{t("user.mail")}</th>
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

export default withTranslation("users")(UserTable);
