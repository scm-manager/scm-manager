import React, { Component } from "react";
import Main from "./Main";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { withRouter } from "react-router-dom";
import { fetchMe, getFetchMeFailure, getMe, isAuthenticated, isFetchMePending } from "../modules/auth";
import { ErrorPage, Footer, Header, Loading, PrimaryNavigation } from "@scm-manager/ui-components";
import { Links, Me } from "@scm-manager/ui-types";
import {
  getAppVersion,
  getFetchIndexResourcesFailure,
  getLinks,
  getMeLink,
  isFetchIndexResourcesPending
} from "../modules/indexResource";

type Props = WithTranslation & {
  me: Me;
  authenticated: boolean;
  error: Error;
  loading: boolean;
  links: Links;
  meLink: string;
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
    const { me, loading, error, authenticated, links, version, t } = this.props;

    let content;
    const navigation = authenticated ? <PrimaryNavigation links={links} /> : "";

    if (loading) {
      content = <Loading />;
    } else if (error) {
      content = <ErrorPage title={t("app.error.title")} subtitle={t("app.error.subtitle")} error={error} />;
    } else {
      content = <Main authenticated={authenticated} links={links} />;
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
  const authenticated = isAuthenticated(state);
  const me = getMe(state);
  const loading = isFetchMePending(state) || isFetchIndexResourcesPending(state);
  const error = getFetchMeFailure(state) || getFetchIndexResourcesFailure(state);
  const links = getLinks(state);
  const meLink = getMeLink(state);
  const version = getAppVersion(state);
  return {
    authenticated,
    me,
    loading,
    error,
    links,
    meLink,
    version
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps), withTranslation("commons"))(App);
