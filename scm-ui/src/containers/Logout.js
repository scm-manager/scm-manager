//@flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Redirect } from "react-router-dom";

import {
  logout,
  isAuthenticated,
  isLogoutPending,
  getLogoutFailure
} from "../modules/auth";
import { Loading, ErrorPage } from "@scm-manager/ui-components";
import { getLogoutLink } from "../modules/indexResource";

type Props = {
  authenticated: boolean,
  loading: boolean,
  error: Error,
  logoutLink: string,

  // dispatcher functions
  logout: (link: string) => void,

  // context props
  t: string => string
};

class Logout extends React.Component<Props> {
  componentDidMount() {
    this.props.logout(this.props.logoutLink);
  }

  render() {
    const { authenticated, loading, error, t } = this.props;
    if (error) {
      return (
        <ErrorPage
          title={t("logout.error.title")}
          subtitle={t("logout.error.subtitle")}
          error={error}
        />
      );
    } else if (loading || authenticated) {
      return <Loading />;
    } else {
      return <Redirect to="/login" />;
    }
  }
}

const mapStateToProps = state => {
  const authenticated = isAuthenticated(state);
  const loading = isLogoutPending(state);
  const error = getLogoutFailure(state);
  const logoutLink = getLogoutLink(state);
  return {
    authenticated,
    loading,
    error,
    logoutLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    logout: (link: string) => dispatch(logout(link))
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("commons")(Logout));
