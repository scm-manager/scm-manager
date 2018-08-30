// @flow
import * as React from "react";
import Loading from "./Loading";
import { apiClient } from "../apiclient";

type Props = {
  children: React.Node
};

type State = {
  finished: boolean,
  message: string
};

type Plugin = {
  id: string,
  bundles: string[]
};

class PluginLoader extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      finished: false,
      message: "booting"
    };
  }

  componentDidMount() {
    this.setState({
      message: "loading plugin information"
    });
    apiClient
      .get("ui/plugins")
      .then(response => response.text())
      .then(JSON.parse)
      .then(pluginCollection => pluginCollection._embedded.plugins)
      .then(this.loadPlugins)
      .then(() => {
        this.setState({
          finished: true
        });
      });
  }

  loadPlugins = (plugins: Plugin[]) => {
    this.setState({
      message: "loading plugins"
    });

    const promises = [];
    for (let plugin of plugins) {
      promises.push(this.loadPlugin(plugin));
    }
    return Promise.all(promises);
  };

  loadPlugin = (plugin: Plugin) => {
    this.setState({
      message: `loading ${plugin.name}`
    });

    const promises = [];
    for (let bundle of plugin.bundles) {
      promises.push(this.loadBundle(bundle));
    }
    return Promise.all(promises);
  };

  loadBundle = (bundle: string) => {
    return fetch(bundle)
      .then(response => {
        return response.text();
      })
      .then(script => {
        // TODO is this safe???
        // NOSONAR
        eval(script);
      });
  };

  render() {
    const { message, finished } = this.state;
    if (finished) {
      return <div>{this.props.children}</div>;
    }
    return <Loading message={message} />;
  }
}

export default PluginLoader;
