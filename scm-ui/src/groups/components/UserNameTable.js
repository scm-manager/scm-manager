//@flow
import React from "react";
import Button from "../../components/buttons/Button"
import { translate } from "react-i18next"
import RemoveUserButton from "./buttons/RemoveUserButton";

type Props = {
  users: string[];
  t: string => string,
  userListChanged: (string[]) => void
};

type State = {
};


class UserNameTable extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <div>
        <label className="label">{t("group.members")}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {this.props.users.map((user, index) => {
              return (
                <tr key={user}>
                  <td key={user}>{user}</td>
                  <td>
                    <RemoveUserButton username={user} removeUser={this.removeUser} />
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
    const newUsers = this.props.users.filter(name => name !== username);
    this.props.userListChanged(newUsers);
  }
}

export default translate("groups")(UserNameTable);
