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
import classNames from "classnames";
import styled from "styled-components";
import { apiClient, ErrorBoundary, ErrorNotification, Icon, Loading } from "@scm-manager/ui-components";
import loadBundle from "./loadBundle";
import { ExtensionPoint } from "@scm-manager/ui-extensions";

const isMainModuleBundle = (bundlePath: string, pluginName: string) => bundlePath.endsWith(`${pluginName}.bundle.js`);

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

class PluginLoader extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      message: "booting",
    };
  }

  componentDidMount() {
    const { loaded } = this.props;
    if (!loaded) {
      this.setState({
        message: "loading plugin information",
      });

      this.getPlugins(this.props.link);
    }
  }

  getPlugins = (link: string) => {
    apiClient
      .get(link)
      .then((response) => response.text())
      .then(JSON.parse)
      .then((pluginCollection) => pluginCollection._embedded.plugins)
      .then(this.loadPlugins)
      .then(this.props.callback);
  };

  loadPlugins = (plugins: Plugin[]) => {
    this.setState({
      message: "loading plugins",
    });

    const promises = [];
    const sortedPlugins = [...plugins].sort(comparePluginsByName);
    for (const plugin of sortedPlugins) {
      promises.push(this.loadPlugin(plugin));
    }
    return Promise.all(promises);
  };

  loadPlugin = (plugin: Plugin) => {
    const promises = [];
    for (const bundlePath of plugin.bundles) {
      promises.push(
        (isMainModuleBundle(bundlePath, plugin.name)
          ? loadBundle(bundlePath, plugin.name)
          : loadBundle(bundlePath)
        ).catch((error) => this.setState({ error, errorMessage: `loading ${plugin.name} failed` }))
      );
    }
    return Promise.all(promises);
  };

  render() {
    const { loaded, children } = this.props;
    const { message, error, errorMessage } = this.state;

    if (error) {
      return (
        <section className="section">
          <div className="container">
            <ErrorBoundary>
              <div
                className={classNames(
                  "is-flex",
                  "is-flex-direction-column",
                  "is-justify-content-space-between",
                  "is-align-items-center"
                )}
              >
                <BigIcon name="exclamation-triangle" color="danger" />
                <div className={classNames("my-5", "is-size-5")}>{errorMessage}</div>
              </div>
              <ErrorNotification error={error} />
            </ErrorBoundary>
          </div>
        </section>
      );
    }

    if (loaded) {
      return <ExtensionPoint name="main.wrapper">{children}</ExtensionPoint>;
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

export default PluginLoader;
