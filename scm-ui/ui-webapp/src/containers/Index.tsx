import React, { Component } from "react";
import App from "./App";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { withRouter } from "react-router-dom";
import { Loading, ErrorBoundary } from "@scm-manager/ui-components";
import {
  fetchIndexResources,
  getFetchIndexResourcesFailure,
  getLinks,
  isFetchIndexResourcesPending
} from "../modules/indexResource";
import PluginLoader from "./PluginLoader";
import { IndexResources } from "@scm-manager/ui-types";
import ScrollToTop from "./ScrollToTop";
import IndexErrorPage from "./IndexErrorPage";

type Props = WithTranslation & {
  error: Error;
  loading: boolean;
  indexResources: IndexResources;

  // dispatcher functions
  fetchIndexResources: () => void;
};

type State = {
  pluginsLoaded: boolean;
};

class Index extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      pluginsLoaded: false
    };
  }

  componentDidMount() {
    this.props.fetchIndexResources();
  }

  pluginLoaderCallback = () => {
    this.setState({
      pluginsLoaded: true
    });
  };

  render() {
    const { indexResources, loading, error } = this.props;
    const { pluginsLoaded } = this.state;

    if (error) {
      return <IndexErrorPage error={error} />;
    } else if (loading || !indexResources) {
      return <Loading />;
    } else {
      return (
        <ErrorBoundary fallback={IndexErrorPage}>
          <ScrollToTop>
            <PluginLoader loaded={pluginsLoaded} callback={this.pluginLoaderCallback}>
              <App />
            </PluginLoader>
          </ScrollToTop>
        </ErrorBoundary>
      );
    }
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchIndexResources: () => dispatch(fetchIndexResources())
  };
};

const mapStateToProps = (state: any) => {
  const loading = isFetchIndexResourcesPending(state);
  const error = getFetchIndexResourcesFailure(state);
  const indexResources = getLinks(state);
  return {
    loading,
    error,
    indexResources
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps), withTranslation("commons"))(Index);
