// @flow
import * as React from "react";
import { apiClient, Loading } from "@scm-manager/ui-components";
import { getUiPluginsLink } from "../modules/indexResource";
import { connect } from "react-redux";

type Props = {
  loaded: boolean,
  children: React.Node,
  link: string,
  callback: () => void
};

type State = {
  message: string
};

type Plugin = {
  name: string,
  bundles: string[]
};

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
    const sortedPlugins = plugins.sort(comparePluginsByName);
    for (let plugin of sortedPlugins) {
      promises.push(this.loadPlugin(plugin));
    }
    return promises.reduce((chain, current) => {
      return chain.then(chainResults => {
          return current.then(currentResult => [...chainResults, currentResult])
        }
      );
    }, Promise.resolve([]));
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
    return fetch(bundle, {
      credentials: "same-origin",
      headers: {
        Cache: "no-cache",
        // identify the request as ajax request
        "X-Requested-With": "XMLHttpRequest"
      }
    })
      .then(response => {
        return response.text();
      })
      .then(script => {
        // TODO is this safe???
        // eslint-disable-next-line no-eval
        eval(script); // NOSONAR
      });
  };

  render() {
    const { loaded } = this.props;
    const { message } = this.state;
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
const mapStateToProps = state => {
  const link = getUiPluginsLink(state);
  return {
    link
  };
};

export default connect(mapStateToProps)(PluginLoader);
