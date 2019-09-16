// @flow
import * as types from "../../../modules/types";
import { isPending } from "../../../modules/pending";
import { getFailure } from "../../../modules/failure";
import type { Action, Plugin, PluginCollection } from "@scm-manager/ui-types";
import { apiClient } from "@scm-manager/ui-components";

export const FETCH_PLUGINS = "scm/plugins/FETCH_PLUGINS";
export const FETCH_PLUGINS_PENDING = `${FETCH_PLUGINS}_${types.PENDING_SUFFIX}`;
export const FETCH_PLUGINS_SUCCESS = `${FETCH_PLUGINS}_${types.SUCCESS_SUFFIX}`;
export const FETCH_PLUGINS_FAILURE = `${FETCH_PLUGINS}_${types.FAILURE_SUFFIX}`;

export const FETCH_PLUGIN = "scm/plugins/FETCH_PLUGIN";
export const FETCH_PLUGIN_PENDING = `${FETCH_PLUGIN}_${types.PENDING_SUFFIX}`;
export const FETCH_PLUGIN_SUCCESS = `${FETCH_PLUGIN}_${types.SUCCESS_SUFFIX}`;
export const FETCH_PLUGIN_FAILURE = `${FETCH_PLUGIN}_${types.FAILURE_SUFFIX}`;

export const FETCH_PENDING_PLUGINS = "scm/plugins/FETCH_PENDING_PLUGINS";
export const FETCH_PENDING_PLUGINS_PENDING = `${FETCH_PENDING_PLUGINS}_${
  types.PENDING_SUFFIX
}`;
export const FETCH_PENDING_PLUGINS_SUCCESS = `${FETCH_PENDING_PLUGINS}_${
  types.SUCCESS_SUFFIX
}`;
export const FETCH_PENDING_PLUGINS_FAILURE = `${FETCH_PENDING_PLUGINS}_${
  types.FAILURE_SUFFIX
}`;

// fetch plugins
export function fetchPluginsByLink(link: string) {
  return function(dispatch: any) {
    dispatch(fetchPluginsPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(plugins => {
        dispatch(fetchPluginsSuccess(plugins));
      })
      .catch(err => {
        dispatch(fetchPluginsFailure(err));
      });
  };
}

export function fetchPluginsPending(): Action {
  return {
    type: FETCH_PLUGINS_PENDING
  };
}

export function fetchPluginsSuccess(plugins: PluginCollection): Action {
  return {
    type: FETCH_PLUGINS_SUCCESS,
    payload: plugins
  };
}

export function fetchPluginsFailure(err: Error): Action {
  return {
    type: FETCH_PLUGINS_FAILURE,
    payload: err
  };
}

// fetch plugin
export function fetchPluginByLink(plugin: Plugin) {
  return fetchPlugin(plugin._links.self.href, plugin.name);
}

export function fetchPluginByName(link: string, name: string) {
  const pluginUrl = link.endsWith("/") ? link : link + "/";
  return fetchPlugin(pluginUrl + name, name);
}

function fetchPlugin(link: string, name: string) {
  return function(dispatch: any) {
    dispatch(fetchPluginPending(name));
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(plugin => {
        dispatch(fetchPluginSuccess(plugin));
      })
      .catch(err => {
        dispatch(fetchPluginFailure(name, err));
      });
  };
}

export function fetchPluginPending(name: string): Action {
  return {
    type: FETCH_PLUGIN_PENDING,
    payload: {
      name
    },
    itemId: name
  };
}

export function fetchPluginSuccess(plugin: Plugin): Action {
  return {
    type: FETCH_PLUGIN_SUCCESS,
    payload: plugin,
    itemId: plugin.name
  };
}

export function fetchPluginFailure(name: string, error: Error): Action {
  return {
    type: FETCH_PLUGIN_FAILURE,
    payload: {
      name,
      error
    },
    itemId: name
  };
}

// fetch pending plugins
export function fetchPendingPlugins(link: string) {
  return function(dispatch: any) {
    dispatch(fetchPendingPluginsPending());
    return apiClient
      .get(link)
      .then(response => response.json())
      .then(PendingPlugins => {
        dispatch(fetchPendingPluginsSuccess(PendingPlugins));
      })
      .catch(err => {
        dispatch(fetchPendingPluginsFailure(err));
      });
  };
}

export function fetchPendingPluginsPending(): Action {
  return {
    type: FETCH_PENDING_PLUGINS_PENDING
  };
}

export function fetchPendingPluginsSuccess(PendingPlugins: {}): Action {
  return {
    type: FETCH_PENDING_PLUGINS_SUCCESS,
    payload: PendingPlugins
  };
}

export function fetchPendingPluginsFailure(err: Error): Action {
  return {
    type: FETCH_PENDING_PLUGINS_FAILURE,
    payload: err
  };
}

// reducer
function normalizeByName(state: Object, pluginCollection: PluginCollection) {
  const names = [];
  const byNames = {};
  for (const plugin of pluginCollection._embedded.plugins) {
    names.push(plugin.name);
    byNames[plugin.name] = plugin;
  }
  return {
    ...state,
    list: {
      ...pluginCollection,
      _embedded: {
        plugins: names
      }
    },
    byNames: byNames
  };
}

const reducerByNames = (state: Object, plugin: Plugin) => {
  return {
    ...state,
    byNames: {
      ...state.byNames,
      [plugin.name]: plugin
    }
  };
};

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
  if (!action.payload) {
    return state;
  }

  switch (action.type) {
    case FETCH_PLUGINS_SUCCESS:
      return normalizeByName(state, action.payload);
    case FETCH_PLUGIN_SUCCESS:
      return reducerByNames(state, action.payload);
    case FETCH_PENDING_PLUGINS_SUCCESS:
      return { ...state, pending: action.payload };
    default:
      return state;
  }
}

// selectors
export function getPluginCollection(state: Object) {
  if (state.plugins && state.plugins.list && state.plugins.byNames) {
    const plugins = [];
    for (let pluginName of state.plugins.list._embedded.plugins) {
      plugins.push(state.plugins.byNames[pluginName]);
    }
    return {
      ...state.plugins.list,
      _embedded: {
        plugins
      }
    };
  }
}

export function isFetchPluginsPending(state: Object) {
  return isPending(state, FETCH_PLUGINS);
}

export function getFetchPluginsFailure(state: Object) {
  return getFailure(state, FETCH_PLUGINS);
}

export function getPlugin(state: Object, name: string) {
  if (state.plugins && state.plugins.byNames) {
    return state.plugins.byNames[name];
  }
}

export function isFetchPluginPending(state: Object, name: string) {
  return isPending(state, FETCH_PLUGIN, name);
}

export function getFetchPluginFailure(state: Object, name: string) {
  return getFailure(state, FETCH_PLUGIN, name);
}

export function getPendingPlugins(state: Object) {
  if (state.plugins && state.plugins.pending) {
    return state.plugins.pending;
  }
}

export function isFetchPendingPluginsPending(state: Object) {
  return isPending(state, FETCH_PENDING_PLUGINS);
}

export function getFetchPendingPluginsFailure(state: Object) {
  return getFailure(state, FETCH_PENDING_PLUGINS);
}
