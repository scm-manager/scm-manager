/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { Redirect } from "react-router-dom";

import { getLogoutFailure, isLogoutPending, isRedirecting, logout } from "../modules/auth";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { getLoginLink, getLogoutLink } from "../modules/indexResource";

type Props = WithTranslation & {
  authenticated: boolean;
  loading: boolean;
  redirecting: boolean;
  error: Error;
  logoutLink: string;

  // dispatcher functions
  logout: (link: string) => void;
};

class Logout extends React.Component<Props> {
  componentDidMount() {
    if (this.props.logoutLink) {
      this.props.logout(this.props.logoutLink);
    }
  }

  render() {
    const { authenticated, redirecting, loading, error, t } = this.props;
    if (error) {
      return <ErrorPage title={t("logout.error.title")} subtitle={t("logout.error.subtitle")} error={error} />;
    } else if (loading || authenticated || redirecting) {
      return <Loading />;
    } else {
      return <Redirect to="/login" />;
    }
  }
}

const mapStateToProps = (state: any) => {
  const authenticated = state.auth.me && !getLoginLink(state);
  const loading = isLogoutPending(state);
  const redirecting = isRedirecting(state);
  const error = getLogoutFailure(state);
  const logoutLink = getLogoutLink(state);
  return {
    authenticated,
    loading,
    redirecting,
    error,
    logoutLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    logout: (link: string) => dispatch(logout(link))
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("commons")(Logout));
