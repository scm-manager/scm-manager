//@flow
import React from "react";
import type { User } from "../types/User";
import { translate } from "react-i18next";
import Checkbox from "../../components/Checkbox";

type Props = {
  user: User,
  t: string => string
};

class Details extends React.Component<Props> {
  render() {
    const { user, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <td>{t("user.name")}</td>
            <td>{user.name}</td>
          </tr>
          <tr>
            <td>{t("user.displayName")}</td>
            <td>{user.displayName}</td>
          </tr>
          <tr>
            <td>{t("user.mail")}</td>
            <td>{user.mail}</td>
          </tr>
          <tr>
            <td>{t("user.admin")}</td>
            <td>
              <Checkbox checked={user.admin} />
            </td>
          </tr>
          <tr>
            <td>{t("user.active")}</td>
            <td>
              <Checkbox checked={user.active} />
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("users")(Details);
