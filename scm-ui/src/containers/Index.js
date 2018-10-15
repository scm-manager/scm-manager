// @flow
import React, { Component } from "react";
import App from "./App";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { withRouter } from "react-router-dom";

import { Loading, ErrorPage } from "@scm-manager/ui-components";
import {
  fetchIndexResources,
  getFetchIndexResourcesFailure,
  getLinks,
  isFetchIndexResourcesPending
} from "../modules/indexResource";
import PluginLoader from "./PluginLoader";

type Props = {
  error: Error,
  loading: boolean,
  indexResources: any,

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
    const { indexResources, loading, error, t } = this.props;

  if (error) {
      return (
        <ErrorPage
          title={t("app.error.title")}
          subtitle={t("app.error.subtitle")}
          error={error}
        />
      );
    }
    else if (loading || !indexResources) {
      return <Loading />;
    }  else {
      return (
        <PluginLoader>
          <App />
        </PluginLoader>
      );
    }
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchIndexResources: () => dispatch(fetchIndexResources())
  };
};

const mapStateToProps = state => {
  const loading = isFetchIndexResourcesPending(state);
  const error = getFetchIndexResourcesFailure(state);
  const indexResources = getLinks(state);
  return {
    loading,
    error,
    indexResources
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("commons")(Index))
);
