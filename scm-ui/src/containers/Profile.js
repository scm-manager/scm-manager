// @flow

import React from "react";

import {
  Page,
  Navigation,
  Section,
  MailLink
} from "../../../scm-ui-components/packages/ui-components/src/index";
import { NavLink, Route } from "react-router-dom";
import { getMe } from "../modules/auth";
import { compose } from "redux";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { Me } from "../../../scm-ui-components/packages/ui-types/src/index";
import AvatarWrapper from "../repos/components/changesets/AvatarWrapper";
import { ErrorPage } from "@scm-manager/ui-components";
import ChangeUserPassword from "./ChangeUserPassword";
import ProfileInfo from "./ProfileInfo";

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
        <Route path={""} render={() => <ProfileInfo me={me} />} />
        <Route path={"/password"} component={ChangeUserPassword} />
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
