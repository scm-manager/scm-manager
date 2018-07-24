// @flow
import React from "react";
import { translate } from "react-i18next";
import UserRow from "./UserRow";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  t: string => string,
  entries: Array<UserEntry>,
  deleteUser: User => void
};

class UserTable extends React.Component<Props> {
  render() {
    const { deleteUser, t } = this.props;
    const entries = this.props.entries;
    return (
      <table className="table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("user.name")}</th>
            <th>{t("user.displayName")}</th>
            <th>{t("user.mail")}</th>
            <th>{t("user.admin")}</th>
            <th />
            <th />
          </tr>
        </thead>
        <tbody>
          {entries.map((entry, index) => {
            return (
              <UserRow key={index} entry={entry} deleteUser={deleteUser} />
            );
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("users")(UserTable);
