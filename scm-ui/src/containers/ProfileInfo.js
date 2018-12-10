// @flow
import React from "react";
import type { Me } from "@scm-manager/ui-types";
import { MailLink, AvatarWrapper, AvatarImage } from "@scm-manager/ui-components";
import { compose } from "redux";
import { translate } from "react-i18next";

type Props = {
  me: Me,

  // Context props
  t: string => string
};
type State = {};

class ProfileInfo extends React.Component<Props, State> {
  render() {
    const { me, t } = this.props;
    return (
      <div className="media">
        <AvatarWrapper>
          <figure className="media-left">
            <p className="image is-64x64">
              <AvatarImage person={ me }/>
            </p>
          </figure>
        </AvatarWrapper>
        <div className="media-content">
          <table className="table">
            <tbody>
              <tr>
                <td>{t("profile.username")}</td>
                <td>{me.name}</td>
              </tr>
              <tr>
                <td>{t("profile.displayName")}</td>
                <td>{me.displayName}</td>
              </tr>
              <tr>
                <td>{t("profile.mail")}</td>
                <td>
                  <MailLink address={me.mail} />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    );
  }
}

export default compose(translate("commons"))(ProfileInfo);
