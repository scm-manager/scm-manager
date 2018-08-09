//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  FETCH_CONFIG,
  FETCH_CONFIG_PENDING,
  FETCH_CONFIG_SUCCESS,
  FETCH_CONFIG_FAILURE,
  fetchConfig,
  fetchConfigSuccess,
  getFetchConfigFailure,
  isFetchConfigPending
} from "./config";

const CONFIG_URL = "/scm/api/rest/v2/config";

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
  baseUrl: "http://localhost:8081/scm",
  forceBaseUrl: false,
  loginAttemptLimit: -1,
  proxyExcludes: [],
  skipFailedAuthenticators: false,
  pluginUrl:
    "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false",
  loginAttemptLimitTimeout: 300,
  enabledXsrfProtection: true,
  defaultNamespaceStrategy: "sonia.scm.repository.DefaultNamespaceStrategy",
  _links: {
    self: { href: "http://localhost:8081/scm/api/rest/v2/config" },
    update: { href: "http://localhost:8081/scm/api/rest/v2/config" }
  }
};

const responseBody = {
  entries: config
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
    fetchMock.getOnce(CONFIG_URL, response);

    const expectedActions = [
      { type: FETCH_CONFIG_PENDING },
      {
        type: FETCH_CONFIG_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchConfig()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting config on HTTP 500", () => {
    fetchMock.getOnce(CONFIG_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchConfig()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_CONFIG_PENDING);
      expect(actions[1].type).toEqual(FETCH_CONFIG_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("config reducer", () => {
  it("should update state correctly according to FETCH_CONFIG_SUCCESS action", () => {
    const newState = reducer({}, fetchConfigSuccess(config));

    expect(newState.config).toEqual({
      entries: config,
      configUpdatePermission: true
    });
  });

  it("should set configUpdatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchConfigSuccess(config));

    expect(newState.config.configUpdatePermission).toBeTruthy();
  });

  it("should update state according to FETCH_GROUP_SUCCESS action", () => {
    const newState = reducer({}, fetchConfigSuccess(config));
    expect(newState.config.entries).toBe(config);
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
});
