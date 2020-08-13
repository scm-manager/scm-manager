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
import React, { ReactNode } from "react";
import { apiClient, Loading, ErrorNotification, ErrorBoundary, Icon } from "@scm-manager/ui-components";
import { getUiPluginsLink } from "../modules/indexResource";
import { connect } from "react-redux";
import loadBundle from "./loadBundle";
import styled from "styled-components";

type Props = {
  loaded: boolean;
  children: ReactNode;
  link: string;
  callback: () => void;
};

type State = {
  message: string;
  errorMessage?: string;
  error?: Error;
};

type Plugin = {
  name: string;
  bundles: string[];
};

const BigIcon = styled(Icon)`
  font-size: 10rem;
`;

const Centered = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
`;

const ErrorMessage = styled.span`
  font-size: 20px;
  margin: 1.5rem 0;
`;

class PluginLoader extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      message: "booting"
    };
  }

  componentDidMount() {
    const { loaded } = this.props;
    if (!loaded) {
      this.setState({
        message: "loading plugin information"
      });

      this.getPlugins(this.props.link);
    }
  }

  getPlugins = (link: string): Promise<any> => {
    return apiClient
      .get(link)
      .then(response => response.text())
      .then(JSON.parse)
      .then(pluginCollection => pluginCollection._embedded.plugins)
      .then(this.loadPlugins)
      .then(this.props.callback);
  };

  loadPlugins = (plugins: Plugin[]) => {
    this.setState({
      message: "loading plugins"
    });

    const promises = [];
    const sortedPlugins = [...plugins].sort(comparePluginsByName);
    for (const plugin of sortedPlugins) {
      promises.push(this.loadPlugin(plugin));
    }
    return promises.reduce((chain, current) => {
      return chain.then(chainResults => {
        return current.then(currentResult => [...chainResults, currentResult]);
      });
    }, Promise.resolve([]));
  };

  loadPlugin = (plugin: Plugin) => {
    this.setState({
      message: `loading ${plugin.name}`
    });

    const promises = [];
    for (const bundle of plugin.bundles) {
      promises.push(
        loadBundle(bundle).catch(error => this.setState({ error, errorMessage: `loading ${plugin.name} failed` }))
      );
    }
    return Promise.all(promises);
  };

  render() {
    const { loaded } = this.props;
    const { message, error, errorMessage } = this.state;

    if (error) {
      return (
        <section className="section">
          <div className="container">
            <ErrorBoundary>
              <Centered>
                <BigIcon name="exclamation-triangle" color="danger" />
                <ErrorMessage>{errorMessage}</ErrorMessage>
              </Centered>
              <ErrorNotification error={error} />
            </ErrorBoundary>
          </div>
        </section>
      );
    }

    if (loaded) {
      return <div>{this.props.children}</div>;
    }
    return <Loading message={message} />;
  }
}
const comparePluginsByName = (a: Plugin, b: Plugin) => {
  if (a.name < b.name) {
    return -1;
  }
  if (a.name > b.name) {
    return 1;
  }
  return 0;
};

const mapStateToProps = (state: any) => {
  const link = getUiPluginsLink(state);
  return {
    link
  };
};

export default connect(mapStateToProps)(PluginLoader);
