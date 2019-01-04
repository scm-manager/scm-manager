// @flow
import React, { Component } from "react";
import Main from "./Main";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { withRouter } from "react-router-dom";
import {
  fetchMe,
  isAuthenticated,
  getMe,
  isFetchMePending,
  getFetchMeFailure
} from "../modules/auth";

import {
  PrimaryNavigation,
  Loading,
  ErrorPage,
  Footer,
  Header
} from "@scm-manager/ui-components";
import type { Links, Me } from "@scm-manager/ui-types";
import {
  getFetchIndexResourcesFailure,
  getLinks,
  getMeLink,
  isFetchIndexResourcesPending
} from "../modules/indexResource";

type Props = {
  me: Me,
  authenticated: boolean,
  error: Error,
  loading: boolean,
  links: Links,
  meLink: string,

  // dispatcher functions
  fetchMe: (link: string) => void,

  // context props
  t: string => string
};

class App extends Component<Props> {
  componentDidMount() {
    if (this.props.meLink) {
      this.props.fetchMe(this.props.meLink);
    }
  }

  render() {
    const {
      me,
      loading,
      error,
      authenticated,
      links,
      t
    } = this.props;

    let content;
    const navigation = authenticated ? (
      <PrimaryNavigation
        links={links}
      />
    ) : (
      ""
    );

    if (loading) {
      content = <Loading />;
    } else if (error) {
      content = (
        <ErrorPage
          title={t("app.error.title")}
          subtitle={t("app.error.subtitle")}
          error={error}
        />
      );
    } else {
      content = <Main authenticated={authenticated} />;
    }
    return (
      <div className="App">
        <Header>{navigation}</Header>
        {content}
        <Footer me={me} />
      </div>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchMe: (link: string) => dispatch(fetchMe(link))
  };
};

const mapStateToProps = state => {
  const authenticated = isAuthenticated(state);
  const me = getMe(state);
  const loading =
    isFetchMePending(state) || isFetchIndexResourcesPending(state);
  const error =
    getFetchMeFailure(state) || getFetchIndexResourcesFailure(state);
  const links = getLinks(state);
  const meLink = getMeLink(state);
  return {
    authenticated,
    me,
    loading,
    error,
    links,
    meLink
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("commons")(App))
);
