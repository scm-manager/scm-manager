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
import App from "./App";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { withRouter } from "react-router-dom";
import { ErrorBoundary, Loading } from "@scm-manager/ui-components";
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

  componentDidUpdate() {
    const { indexResources, loading, error } = this.props;
    const { pluginsLoaded } = this.state;
    if (!indexResources && !loading && !error && pluginsLoaded) {
      this.props.fetchIndexResources();
      this.setState({ pluginsLoaded: false });
    }
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
