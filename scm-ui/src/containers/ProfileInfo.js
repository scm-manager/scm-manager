// @flow
import React from "react";
import AvatarWrapper from "../repos/components/changesets/AvatarWrapper";
import { NavLink } from "react-router-dom";
import type { Me } from "@scm-manager/ui-types";
import { MailLink, Navigation, Section } from "@scm-manager/ui-components";
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
      <div className="columns">
        <AvatarWrapper>
          <div>
            <figure className="media-left">
              <p className="image is-64x64">
                {
                  // TODO: add avatar
                }
              </p>
            </figure>
          </div>
        </AvatarWrapper>
        <div className="column is-two-quarters">
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
        <div className="column is-one-quarter">
          <Navigation>
            <Section label={t("profile.actions-label")} />
            <NavLink to={"password"}>{t("profile.change-password")}</NavLink>
          </Navigation>
        </div>
      </div>
    );
  }
}

export default compose(translate("commons"))(ProfileInfo);
