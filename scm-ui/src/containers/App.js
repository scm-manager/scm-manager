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

import "./App.css";
import "../components/modals/ConfirmAlert.css";
import { PrimaryNavigation } from "../components/navigation";
import Loading from "../components/Loading";
import ErrorPage from "../components/ErrorPage";
import { Footer, Header } from "../components/layout";

type Props = {
  me: Me,
  authenticated: boolean,
  error: Error,
  loading: boolean,

  // dispatcher functions
  fetchMe: () => void,

  // context props
  t: string => string
};

class App extends Component<Props> {
  componentDidMount() {
    this.props.fetchMe();
  }

  render() {
    const { me, loading, error, authenticated, t } = this.props;

    let content;
    const navigation = authenticated ? <PrimaryNavigation /> : "";

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
    fetchMe: () => dispatch(fetchMe())
  };
};

const mapStateToProps = state => {
  const authenticated = isAuthenticated(state);
  const me = getMe(state);
  const loading = isFetchMePending(state);
  const error = getFetchMeFailure(state);
  return {
    authenticated,
    me,
    loading,
    error
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("commons")(App))
);
