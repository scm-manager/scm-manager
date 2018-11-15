// @flow

import React from "react";

import {
  Page,
  Navigation,
  Section,
  MailLink
} from "../../../scm-ui-components/packages/ui-components/src/index";
import { NavLink } from "react-router-dom";
import { getMe } from "../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Me } from "../../../scm-ui-components/packages/ui-types/src/index";
import AvatarWrapper from "../repos/components/changesets/AvatarWrapper";
import { ErrorPage } from "@scm-manager/ui-components";

type Props = {
  me: Me,

  // Context props
  t: string => string
};
type State = {};

class Profile extends React.Component<Props, State> {
  render() {
    const { me, t } = this.props;

    if (me) {
      return (
        <ErrorPage
          title={t("profile.error-title")}
          subtitle={t("profile.error-subtitle")}
          error={{ name: "Error", message: "'me' is undefined" }}
        />
      );
    }
    return (
      <Page title={me.displayName}>
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
              <NavLink to={"me/password"}>
                {t("profile.change-password")}
              </NavLink>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = state => {
  return {
    me: getMe(state)
  };
};

export default compose(
  translate("commons"),
  connect(mapStateToProps)
)(Profile);
