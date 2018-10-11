// @flow
import React, { Component } from "react";
import App from "./App";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { withRouter } from "react-router-dom";
import {
  fetchMe
} from "../modules/auth";

import {
  Loading,
  ErrorPage,
} from "@scm-manager/ui-components";
import {
  fetchIndexResources,
  getFetchIndexResourcesFailure,
  isFetchIndexResourcesPending
} from "../modules/indexResource";

type Props = {
  error: Error,
  loading: boolean,

  // dispatcher functions
  fetchIndexResources: () => void,

  // context props
  t: string => string
};

class Index extends Component<Props> {
  componentDidMount() {
    this.props.fetchIndexResources();
  }

  render() {
    const {
      loading,
      error,
      t,
    } = this.props;

    if (loading) {
      return <Loading />;
    } else if (error) {
      return (
        <ErrorPage
          title={t("app.error.title")}
          subtitle={t("app.error.subtitle")}
          error={error}
        />
      );
    } else {
      return <App />;
    }
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchMe: (link: string) => dispatch(fetchMe(link)),
    fetchIndexResources: () => dispatch(fetchIndexResources())
  };
};

const mapStateToProps = state => {
  const loading = isFetchIndexResourcesPending(state);
  const error = getFetchIndexResourcesFailure(state);
  return {
    loading,
    error
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("commons")(Index))
);
