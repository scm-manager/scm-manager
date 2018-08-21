// @flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  fetchPermissions,
  FETCH_PERMISSIONS_PENDING,
  FETCH_PERMISSIONS_SUCCESS,
  FETCH_PERMISSIONS_FAILURE
} from "./permissions";
import type { Permission, Permissions } from "../types/Permissions";

const s_bPermission_user_eins: Permission = {
  name: "user_eins",
  type: "READ",
  groupPermission: true,
  _links: {
    self: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/s/b/permissions/user_eins"
    },
    delete: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/s/b/permissions/user_eins"
    },
    update: {
      href:
        "http://localhost:8081/scm/api/rest/v2/repositories/s/b/permissions/user_eins"
    }
  }
};

const s_bPermissions: Permissions = [s_bPermission_user_eins];

describe("permission fetch", () => {
  const REPOS_URL = "/scm/api/rest/v2/repositories";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch permissions to repo s/b", () => {
    fetchMock.getOnce(REPOS_URL + "/s/b/permissions", s_bPermissions);

    const expectedActions = [
      {
        type: FETCH_PERMISSIONS_PENDING,
        payload: {
          namespace: "s",
          name: "b"
        },
        itemId: "s/b"
      },
      {
        type: FETCH_PERMISSIONS_SUCCESS,
        payload: s_bPermissions,
        itemId: "s/b"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchPermissions("s", "b")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should dispatch FETCH_PERMISSIONS_FAILURE, it the request fails", () => {
    fetchMock.getOnce(REPOS_URL + "/s/b/permissions", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchPermissions("s", "b")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_PERMISSIONS_PENDING);
      expect(actions[1].type).toEqual(FETCH_PERMISSIONS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});
