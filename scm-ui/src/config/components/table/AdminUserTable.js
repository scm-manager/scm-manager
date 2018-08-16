//@flow
import React from "react";
import { translate } from "react-i18next";
import RemoveAdminUserButton from "../buttons/RemoveAdminUserButton";

type Props = {
  adminUsers: string[],
  t: string => string,
  onChange: (boolean, any, string) => void
};

type State = {};

class AdminUserTable extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <div>
        <label className="label">{t("admin-settings.admin-users")}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {this.props.adminUsers.map(user => {
              return (
                <tr key={user}>
                  <td key={user}>{user}</td>
                  <td>
                    <RemoveAdminUserButton
                      username={user}
                      removeUser={this.removeUser}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeUser = (username: string) => {
    const newUsers = this.props.adminUsers.filter(name => name !== username);
    this.props.onChange(true, newUsers, "adminUsers");
  };
}

export default translate("config")(AdminUserTable);
