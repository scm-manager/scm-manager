//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  FETCH_CONFIG,
  FETCH_CONFIG_PENDING,
  FETCH_CONFIG_SUCCESS,
  FETCH_CONFIG_FAILURE,
  MODIFY_CONFIG,
  MODIFY_CONFIG_PENDING,
  MODIFY_CONFIG_SUCCESS,
  MODIFY_CONFIG_FAILURE,
  fetchConfig,
  fetchConfigSuccess,
  getFetchConfigFailure,
  isFetchConfigPending,
  modifyConfig,
  isModifyConfigPending,
  getModifyConfigFailure,
  getConfig,
  getConfigUpdatePermission
} from "./config";

const CONFIG_URL = "/config";
const URL = "/api/v2" + CONFIG_URL;

const error = new Error("You have an error!");

const config = {
  proxyPassword: null,
  proxyPort: 8080,
  proxyServer: "proxy.mydomain.com",
  proxyUser: null,
  enableProxy: false,
  realmDescription: "SONIA :: SCM Manager",
  enableRepositoryArchive: false,
  disableGroupingGrid: false,
  dateFormat: "YYYY-MM-DD HH:mm:ss",
  anonymousAccessEnabled: false,
  adminGroups: [],
  adminUsers: [],
  baseUrl: "http://localhost:8081",
  forceBaseUrl: false,
  loginAttemptLimit: -1,
  proxyExcludes: [],
  skipFailedAuthenticators: false,
  pluginUrl:
    "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false",
  loginAttemptLimitTimeout: 300,
  enabledXsrfProtection: true,
  namespaceStrategy: "UsernameNamespaceStrategy",
  _links: {
    self: { href: "http://localhost:8081/api/v2/config" },
    update: { href: "http://localhost:8081/api/v2/config" }
  }
};

const configWithNullValues = {
  proxyPassword: null,
  proxyPort: 8080,
  proxyServer: "proxy.mydomain.com",
  proxyUser: null,
  enableProxy: false,
  realmDescription: "SONIA :: SCM Manager",
  enableRepositoryArchive: false,
  disableGroupingGrid: false,
  dateFormat: "YYYY-MM-DD HH:mm:ss",
  anonymousAccessEnabled: false,
  adminGroups: null,
  adminUsers: null,
  baseUrl: "http://localhost:8081",
  forceBaseUrl: false,
  loginAttemptLimit: -1,
  proxyExcludes: null,
  skipFailedAuthenticators: false,
  pluginUrl:
    "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false",
  loginAttemptLimitTimeout: 300,
  enabledXsrfProtection: true,
  namespaceStrategy: "UsernameNamespaceStrategy",
  _links: {
    self: { href: "http://localhost:8081/api/v2/config" },
    update: { href: "http://localhost:8081/api/v2/config" }
  }
};

const responseBody = {
  entries: config,
  configUpdatePermission: false
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};

describe("config fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch config", () => {
    fetchMock.getOnce(URL, response);

    const expectedActions = [
      { type: FETCH_CONFIG_PENDING },
      {
        type: FETCH_CONFIG_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({
      indexResources: {
        links: {
          config: {
            href: CONFIG_URL
          }
        }
      }
    });

    return store.dispatch(fetchConfig(CONFIG_URL)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting config on HTTP 500", () => {
    fetchMock.getOnce(URL, {
      status: 500
    });

    const store = mockStore({
      indexResources: {
        links: {
          config: {
            href: CONFIG_URL
          }
        }
      }
    });
    return store.dispatch(fetchConfig(CONFIG_URL)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_CONFIG_PENDING);
      expect(actions[1].type).toEqual(FETCH_CONFIG_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should successfully modify config", () => {
    fetchMock.putOnce("http://localhost:8081/api/v2/config", {
      status: 204
    });

    const store = mockStore({});

    return store.dispatch(modifyConfig(config)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(MODIFY_CONFIG_PENDING);
      expect(actions[1].type).toEqual(MODIFY_CONFIG_SUCCESS);
      expect(actions[1].payload).toEqual(config);
    });
  });

  it("should call the callback after modifying config", () => {
    fetchMock.putOnce("http://localhost:8081/api/v2/config", {
      status: 204
    });

    let called = false;
    const callback = () => {
      called = true;
    };
    const store = mockStore({});

    return store.dispatch(modifyConfig(config, callback)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(MODIFY_CONFIG_PENDING);
      expect(actions[1].type).toEqual(MODIFY_CONFIG_SUCCESS);
      expect(called).toBe(true);
    });
  });

  it("should fail modifying config on HTTP 500", () => {
    fetchMock.putOnce("http://localhost:8081/api/v2/config", {
      status: 500
    });

    const store = mockStore({});

    return store.dispatch(modifyConfig(config)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(MODIFY_CONFIG_PENDING);
      expect(actions[1].type).toEqual(MODIFY_CONFIG_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("config reducer", () => {
  it("should update state correctly according to FETCH_CONFIG_SUCCESS action", () => {
    const newState = reducer({}, fetchConfigSuccess(config));

    expect(newState).toEqual({
      entries: config,
      configUpdatePermission: true
    });
  });

  it("should set configUpdatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchConfigSuccess(config));

    expect(newState.configUpdatePermission).toBeTruthy();
  });

  it("should update state according to FETCH_CONFIG_SUCCESS action", () => {
    const newState = reducer({}, fetchConfigSuccess(config));
    expect(newState.entries).toBe(config);
  });

  it("should return empty arrays for null values", () => {
    // $FlowFixMe
    const config = reducer({}, fetchConfigSuccess(configWithNullValues))
      .entries;
    expect(config.adminUsers).toEqual([]);
    expect(config.adminGroups).toEqual([]);
    expect(config.proxyExcludes).toEqual([]);
  });
});

describe("selector tests", () => {
  it("should return true, when fetch config is pending", () => {
    const state = {
      pending: {
        [FETCH_CONFIG]: true
      }
    };
    expect(isFetchConfigPending(state)).toEqual(true);
  });

  it("should return false, when fetch config is not pending", () => {
    expect(isFetchConfigPending({})).toEqual(false);
  });

  it("should return error when fetch config did fail", () => {
    const state = {
      failure: {
        [FETCH_CONFIG]: error
      }
    };
    expect(getFetchConfigFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch config did not fail", () => {
    expect(getFetchConfigFailure({})).toBe(undefined);
  });

  it("should return true, when modify group is pending", () => {
    const state = {
      pending: {
        [MODIFY_CONFIG]: true
      }
    };
    expect(isModifyConfigPending(state)).toEqual(true);
  });

  it("should return false, when modify config is not pending", () => {
    expect(isModifyConfigPending({})).toEqual(false);
  });

  it("should return error when modify config did fail", () => {
    const state = {
      failure: {
        [MODIFY_CONFIG]: error
      }
    };
    expect(getModifyConfigFailure(state)).toEqual(error);
  });

  it("should return undefined when modify config did not fail", () => {
    expect(getModifyConfigFailure({})).toBe(undefined);
  });

  it("should return config", () => {
    const state = {
      config: {
        entries: config
      }
    };
    expect(getConfig(state)).toEqual(config);
  });

  it("should return configUpdatePermission", () => {
    const state = {
      config: {
        configUpdatePermission: true
      }
    };
    expect(getConfigUpdatePermission(state)).toEqual(true);
  });
});
