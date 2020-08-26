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
import React, { Component } from "react";
import Main from "./Main";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { withRouter } from "react-router-dom";
import {
  fetchMe,
  getFetchMeFailure,
  getMe,
  isAuthenticated,
  isFetchMePending,
  isLoginPending,
  isLogoutPending
} from "../modules/auth";
import { ErrorPage, Footer, Header, Loading, PrimaryNavigation } from "@scm-manager/ui-components";
import { Links, Me } from "@scm-manager/ui-types";
import {
  getAppVersion,
  getFetchIndexResourcesFailure,
  getLinks,
  getLoginLink,
  getMeLink,
  isFetchIndexResourcesPending
} from "../modules/indexResource";
import Login from "./Login";

type Props = WithTranslation & {
  me: Me;
  authenticated: boolean;
  error: Error;
  loading: boolean;
  links: Links;
  meLink: string;
  loginLink?: string;
  version: string;

  // dispatcher functions
  fetchMe: (link: string) => void;
};

class App extends Component<Props> {
  componentDidMount() {
    if (this.props.meLink) {
      this.props.fetchMe(this.props.meLink);
    }
  }

  render() {
    const { me, loading, error, authenticated, links, loginLink, version, t } = this.props;

    let content;
    const navigation = authenticated ? <PrimaryNavigation links={links} /> : "";

    if (!authenticated && !loading) {
      content = <Login />;
    } else if (loading) {
      content = <Loading />;
    } else if (error) {
      content = <ErrorPage title={t("app.error.title")} subtitle={t("app.error.subtitle")} error={error} />;
    } else {
      content = <Main authenticated={authenticated} links={links} me={me} loginLink={loginLink} />;
    }
    return (
      <div className="App">
        <Header>{navigation}</Header>
        {content}
        {authenticated && <Footer me={me} version={version} links={links} />}
      </div>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchMe: (link: string) => dispatch(fetchMe(link))
  };
};

const mapStateToProps = (state: any) => {
  const authenticated = isAuthenticated(state) && !isLogoutPending(state);
  const me = getMe(state);
  const loading = isFetchMePending(state) || isFetchIndexResourcesPending(state) || isLoginPending(state);
  const error = getFetchMeFailure(state) || getFetchIndexResourcesFailure(state);
  const links = getLinks(state);
  const meLink = getMeLink(state);
  const loginLink = getLoginLink(state);
  const version = getAppVersion(state);
  return {
    authenticated,
    me,
    loading,
    error,
    links,
    meLink,
    loginLink,
    version
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps), withTranslation("commons"))(App);
