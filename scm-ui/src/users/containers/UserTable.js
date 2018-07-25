// @flow
import React from "react";
import { translate } from "react-i18next";
import UserRow from "./UserRow";
import type { UserEntry } from "../types/UserEntry";

type Props = {
  t: string => string,
  entries: Array<UserEntry>
};

class UserTable extends React.Component<Props> {
  render() {
    const { entries, t } = this.props;
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
          {entries.map((entry, index) => {
            return <UserRow key={index} user={entry.entry} />;
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("users")(UserTable);
