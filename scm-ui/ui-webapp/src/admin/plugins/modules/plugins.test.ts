import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import reducer, {
  FETCH_PLUGINS,
  FETCH_PLUGINS_PENDING,
  FETCH_PLUGINS_SUCCESS,
  FETCH_PLUGINS_FAILURE,
  FETCH_PLUGIN,
  FETCH_PLUGIN_PENDING,
  FETCH_PLUGIN_SUCCESS,
  FETCH_PLUGIN_FAILURE,
  fetchPluginsByLink,
  fetchPluginsSuccess,
  getPluginCollection,
  isFetchPluginsPending,
  getFetchPluginsFailure,
  fetchPluginByLink,
  fetchPluginByName,
  fetchPluginSuccess,
  getPlugin,
  isFetchPluginPending,
  getFetchPluginFailure
} from "./plugins";
import { Plugin, PluginCollection } from "@scm-manager/ui-types";

const groupManagerPlugin: Plugin = {
  name: "scm-groupmanager-plugin",
  bundles: ["/scm/groupmanager-plugin.bundle.js"],
  type: "Administration",
  version: "2.0.0-SNAPSHOT",
  author: "Sebastian Sdorra",
  description: "Notify a remote webserver whenever a plugin is pushed to.",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/ui/plugins/scm-groupmanager-plugin"
    }
  }
};

const scriptPlugin: Plugin = {
  name: "scm-script-plugin",
  bundles: ["/scm/script-plugin.bundle.js"],
  type: "Miscellaneous",
  version: "2.0.0-SNAPSHOT",
  author: "Sebastian Sdorra",
  description: "Script support for scm-manager.",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/ui/plugins/scm-script-plugin"
    }
  }
};

const branchwpPlugin: Plugin = {
  name: "scm-branchwp-plugin",
  bundles: ["/scm/branchwp-plugin.bundle.js"],
  type: "Miscellaneous",
  version: "2.0.0-SNAPSHOT",
  author: "Sebastian Sdorra",
  description: "This plugin adds branch write protection for plugins.",
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/ui/plugins/scm-branchwp-plugin"
    }
  }
};

const pluginCollectionWithNames: PluginCollection = {
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/ui/plugins"
    }
  },
  _embedded: {
    plugins: [groupManagerPlugin.name, scriptPlugin.name, branchwpPlugin.name]
  }
};

const pluginCollection: PluginCollection = {
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/ui/plugins"
    }
  },
  _embedded: {
    plugins: [groupManagerPlugin, scriptPlugin, branchwpPlugin]
  }
};

describe("plugins fetch", () => {
  const URL = "ui/plugins";
  const PLUGINS_URL = "/api/v2/ui/plugins";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch plugins from link", () => {
    fetchMock.getOnce(PLUGINS_URL, pluginCollection);

    const expectedActions = [
      {
        type: FETCH_PLUGINS_PENDING
      },
      {
        type: FETCH_PLUGINS_SUCCESS,
        payload: pluginCollection
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchPluginsByLink(URL)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_PLUGINS_FAILURE if request fails", () => {
    fetchMock.getOnce(PLUGINS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchPluginsByLink(URL)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_PLUGINS_PENDING);
      expect(actions[1].type).toEqual(FETCH_PLUGINS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should successfully fetch scm-groupmanager-plugin by name", () => {
    fetchMock.getOnce(PLUGINS_URL + "/scm-groupmanager-plugin", groupManagerPlugin);

    const expectedActions = [
      {
        type: FETCH_PLUGIN_PENDING,
        payload: {
          name: "scm-groupmanager-plugin"
        },
        itemId: "scm-groupmanager-plugin"
      },
      {
        type: FETCH_PLUGIN_SUCCESS,
        payload: groupManagerPlugin,
        itemId: "scm-groupmanager-plugin"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchPluginByName(URL, "scm-groupmanager-plugin")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_PLUGIN_FAILURE, if the request for scm-groupmanager-plugin by name fails", () => {
    fetchMock.getOnce(PLUGINS_URL + "/scm-groupmanager-plugin", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchPluginByName(URL, "scm-groupmanager-plugin")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_PLUGIN_PENDING);
      expect(actions[1].type).toEqual(FETCH_PLUGIN_FAILURE);
      expect(actions[1].payload.name).toBe("scm-groupmanager-plugin");
      expect(actions[1].payload.error).toBeDefined();
      expect(actions[1].itemId).toBe("scm-groupmanager-plugin");
    });
  });

  it("should successfully fetch scm-groupmanager-plugin", () => {
    fetchMock.getOnce("http://localhost:8081/api/v2/ui/plugins/scm-groupmanager-plugin", groupManagerPlugin);

    const expectedActions = [
      {
        type: FETCH_PLUGIN_PENDING,
        payload: {
          name: "scm-groupmanager-plugin"
        },
        itemId: "scm-groupmanager-plugin"
      },
      {
        type: FETCH_PLUGIN_SUCCESS,
        payload: groupManagerPlugin,
        itemId: "scm-groupmanager-plugin"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchPluginByLink(groupManagerPlugin)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_PLUGIN_FAILURE, it the request for scm-groupmanager-plugin fails", () => {
    fetchMock.getOnce("http://localhost:8081/api/v2/ui/plugins/scm-groupmanager-plugin", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchPluginByLink(groupManagerPlugin)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_PLUGIN_PENDING);
      expect(actions[1].type).toEqual(FETCH_PLUGIN_FAILURE);
      expect(actions[1].payload.name).toBe("scm-groupmanager-plugin");
      expect(actions[1].payload.error).toBeDefined();
      expect(actions[1].itemId).toBe("scm-groupmanager-plugin");
    });
  });
});

describe("plugins reducer", () => {
  it("should return empty object, if state and action is undefined", () => {
    expect(reducer()).toEqual({});
  });

  it("should return the same state, if the action is undefined", () => {
    const state = {
      x: true
    };
    expect(reducer(state)).toBe(state);
  });

  it("should return the same state, if the action is unknown to the reducer", () => {
    const state = {
      x: true
    };
    expect(
      reducer(state, {
        type: "EL_SPECIALE"
      })
    ).toBe(state);
  });

  it("should store the plugins by it's type and name on FETCH_PLUGINS_SUCCESS", () => {
    const newState = reducer({}, fetchPluginsSuccess(pluginCollection));
    expect(newState.list._embedded.plugins).toEqual([
      "scm-groupmanager-plugin",
      "scm-script-plugin",
      "scm-branchwp-plugin"
    ]);
    expect(newState.byNames["scm-groupmanager-plugin"]).toBe(groupManagerPlugin);
    expect(newState.byNames["scm-script-plugin"]).toBe(scriptPlugin);
    expect(newState.byNames["scm-branchwp-plugin"]).toBe(branchwpPlugin);
  });

  it("should store the plugin at byNames", () => {
    const newState = reducer({}, fetchPluginSuccess(groupManagerPlugin));
    expect(newState.byNames["scm-groupmanager-plugin"]).toBe(groupManagerPlugin);
  });
});

describe("plugins selectors", () => {
  const error = new Error("something went wrong");

  it("should return the plugins collection", () => {
    const state = {
      plugins: {
        list: pluginCollectionWithNames,
        byNames: {
          "scm-groupmanager-plugin": groupManagerPlugin,
          "scm-script-plugin": scriptPlugin,
          "scm-branchwp-plugin": branchwpPlugin
        }
      }
    };

    const collection = getPluginCollection(state);
    expect(collection).toEqual(pluginCollection);
  });

  it("should return true, when fetch plugins is pending", () => {
    const state = {
      pending: {
        [FETCH_PLUGINS]: true
      }
    };
    expect(isFetchPluginsPending(state)).toEqual(true);
  });

  it("should return false, when fetch plugins is not pending", () => {
    expect(isFetchPluginsPending({})).toEqual(false);
  });

  it("should return error when fetch plugins did fail", () => {
    const state = {
      failure: {
        [FETCH_PLUGINS]: error
      }
    };
    expect(getFetchPluginsFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch plugins did not fail", () => {
    expect(getFetchPluginsFailure({})).toBe(undefined);
  });

  it("should return the plugin collection", () => {
    const state = {
      plugins: {
        byNames: {
          "scm-groupmanager-plugin": groupManagerPlugin
        }
      }
    };

    const plugin = getPlugin(state, "scm-groupmanager-plugin");
    expect(plugin).toEqual(groupManagerPlugin);
  });

  it("should return true, when fetch plugin is pending", () => {
    const state = {
      pending: {
        [FETCH_PLUGIN + "/scm-groupmanager-plugin"]: true
      }
    };
    expect(isFetchPluginPending(state, "scm-groupmanager-plugin")).toEqual(true);
  });

  it("should return false, when fetch plugin is not pending", () => {
    expect(isFetchPluginPending({}, "scm-groupmanager-plugin")).toEqual(false);
  });

  it("should return error when fetch plugin did fail", () => {
    const state = {
      failure: {
        [FETCH_PLUGIN + "/scm-groupmanager-plugin"]: error
      }
    };
    expect(getFetchPluginFailure(state, "scm-groupmanager-plugin")).toEqual(error);
  });

  it("should return undefined when fetch plugin did not fail", () => {
    expect(getFetchPluginFailure({}, "scm-groupmanager-plugin")).toBe(undefined);
  });
});
