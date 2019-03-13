//@flow
import React from "react";
import type { User } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import { Checkbox, MailLink, DateFromNow } from "@scm-manager/ui-components";

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
            <td className="has-text-weight-semibold">{t("user.name")}</td>
            <td>{user.name}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.displayName")}</td>
            <td>{user.displayName}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.mail")}</td>
            <td>
              <MailLink address={user.mail} />
            </td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.active")}</td>
            <td>
              <Checkbox checked={user.active} />
            </td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.type")}</td>
            <td>{user.type}</td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.creationDate")}</td>
            <td>
              <DateFromNow date={user.creationDate} />
            </td>
          </tr>
          <tr>
            <td className="has-text-weight-semibold">{t("user.lastModified")}</td>
            <td>
              <DateFromNow date={user.lastModified} />
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("users")(Details);
