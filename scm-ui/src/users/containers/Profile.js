// @flow

import React from "react";

import {
  Page,
  Navigation,
  Section,
  MailLink
} from "@scm-manager/ui-components";
import { NavLink } from "react-router-dom";
import { getMe } from "../../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Me } from "@scm-manager/ui-types";
import AvatarWrapper from "../../repos/components/changesets/AvatarWrapper";

type Props = {
  me: Me,

  // Context props
  t: string => string
};
type State = {};

class Profile extends React.Component<Props, State> {
  render() {
    const { me, t } = this.props;

    if (!me) {
      return null;
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
                  }
                </p>
              </figure>
            </div>
          </AvatarWrapper>
          <div className="column is-two-quarters">
            <table className="table">
              <tbody>
                <tr>
                  <td>{t("user.name")}</td>
                  <td>{me.name}</td>
                </tr>
                <tr>
                  <td>{t("user.displayName")}</td>
                  <td>{me.displayName}</td>
                </tr>
                <tr>
                  <td>{t("user.mail")}</td>
                  <td>
                    <MailLink address={me.mail} />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div className="column is-one-quarter">
            <Navigation>
              <Section label={t("single-user.actions-label")} />
              <NavLink to={"me/password"}>{t("profile.change-pw")}</NavLink>
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
  translate("users"),
  connect(mapStateToProps)
)(Profile);
