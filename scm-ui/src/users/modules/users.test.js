//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  CREATE_USER_FAILURE,
  CREATE_USER_PENDING,
  CREATE_USER_SUCCESS,
  createUser,
  DELETE_USER_FAILURE,
  DELETE_USER_PENDING,
  DELETE_USER_SUCCESS,
  deleteUser,
  deleteUserSuccess,
  FETCH_USER_FAILURE,
  FETCH_USER_PENDING,
  FETCH_USER_SUCCESS,
  FETCH_USERS_FAILURE,
  FETCH_USERS_PENDING,
  FETCH_USERS_SUCCESS,
  fetchUser,
  fetchUserSuccess,
  fetchUsers,
  fetchUsersSuccess,
  selectListAsCollection,
  isPermittedToCreateUsers,
  MODIFY_USER_FAILURE,
  MODIFY_USER_PENDING,
  MODIFY_USER_SUCCESS,
  modifyUser,
  modifyUserSuccess
} from "./users";

const userZaphod = {
  active: true,
  admin: true,
  creationDate: "2018-07-11T12:23:49.027Z",
  displayName: "Z. Beeblebrox",
  mail: "president@heartofgold.universe",
  name: "zaphod",
  password: "__dummypassword__",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    },
    update: {
      href: "http://localhost:8081/scm/api/rest/v2/users/zaphod"
    }
  }
};

const userFord = {
  active: true,
  admin: false,
  creationDate: "2018-07-06T13:21:18.459Z",
  displayName: "F. Prefect",
  mail: "ford@prefect.universe",
  name: "ford",
  password: "__dummypassword__",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    },
    delete: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    },
    update: {
      href: "http://localhost:8081/scm/api/rest/v2/users/ford"
    }
  }
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/scm/api/rest/v2/users/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/scm/api/rest/v2/users/"
    }
  },
  _embedded: {
    users: [userZaphod, userFord]
  }
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};

const USERS_URL = "/scm/api/rest/v2/users";

describe("users fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch users", () => {
    fetchMock.getOnce(USERS_URL, response);

    const expectedActions = [
      { type: FETCH_USERS_PENDING },
      {
        type: FETCH_USERS_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchUsers()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting users on HTTP 500", () => {
    fetchMock.getOnce(USERS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUsers()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USERS_PENDING);
      expect(actions[1].type).toEqual(FETCH_USERS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single user", () => {
    fetchMock.getOnce(USERS_URL + "/zaphod", userZaphod);

    const store = mockStore({});
    return store.dispatch(fetchUser("zaphod")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USER_PENDING);
      expect(actions[1].type).toEqual(FETCH_USER_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single user on HTTP 500", () => {
    fetchMock.getOnce(USERS_URL + "/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUser("zaphod")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USER_PENDING);
      expect(actions[1].type).toEqual(FETCH_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should add a user successfully", () => {
    // unmatched
    fetchMock.postOnce(USERS_URL, {
      status: 204
    });

    // after create, the users are fetched again
    fetchMock.getOnce(USERS_URL, response);

    const store = mockStore({});
    return store.dispatch(createUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_USER_PENDING);
      expect(actions[1].type).toEqual(CREATE_USER_SUCCESS);
    });
  });

  it("should fail adding a user on HTTP 500", () => {
    fetchMock.postOnce(USERS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(createUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(CREATE_USER_PENDING);
      expect(actions[1].type).toEqual(CREATE_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should call the callback after user successfully created", () => {
    // unmatched
    fetchMock.postOnce(USERS_URL, {
      status: 204
    });

    let callMe = "not yet";

    const callback = () => {
      callMe = "yeah";
    };

    const store = mockStore({});
    return store.dispatch(createUser(userZaphod, callback)).then(() => {
      expect(callMe).toBe("yeah");
    });
  });

  it("successfully update user", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });

    const store = mockStore({});
    return store.dispatch(modifyUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions.length).toBe(2);
      expect(actions[0].type).toEqual(MODIFY_USER_PENDING);
      expect(actions[1].type).toEqual(MODIFY_USER_SUCCESS);
    });
  });

  it("should call callback, after successful modified user", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });

    let called = false;
    const callMe = () => {
      called = true;
    };

    const store = mockStore({});
    return store.dispatch(modifyUser(userZaphod, callMe)).then(() => {
      expect(called).toBeTruthy();
    });
  });

  it("should fail updating user on HTTP 500", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(modifyUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(MODIFY_USER_PENDING);
      expect(actions[1].type).toEqual(MODIFY_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should delete successfully user zaphod", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions.length).toBe(2);
      expect(actions[0].type).toEqual(DELETE_USER_PENDING);
      expect(actions[0].payload).toBe(userZaphod);
      expect(actions[1].type).toEqual(DELETE_USER_SUCCESS);
    });
  });

  it("should call the callback, after successful delete", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });

    let called = false;
    const callMe = () => {
      called = true;
    };

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod, callMe)).then(() => {
      expect(called).toBeTruthy();
    });
  });

  it("should fail to delete user zaphod", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_USER_PENDING);
      expect(actions[0].payload).toBe(userZaphod);
      expect(actions[1].type).toEqual(DELETE_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("users reducer", () => {
  it("should update state correctly according to FETCH_USERS_SUCCESS action", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.list).toEqual({
      entries: ["zaphod", "ford"],
      entry: {
        userCreatePermission: true,
        page: 0,
        pageTotal: 1,
        _links: responseBody._links
      }
    });

    expect(newState.byNames).toEqual({
      zaphod: userZaphod,
      ford: userFord
    });

    expect(newState.list.entry.userCreatePermission).toBeTruthy();
  });

  it("should set userCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.list.entry.userCreatePermission).toBeTruthy();
  });

  it("should not replace whole byNames map when fetching users", () => {
    const oldState = {
      byNames: {
        ford: userFord
      }
    };

    const newState = reducer(oldState, fetchUsersSuccess(responseBody));
    expect(newState.byNames["zaphod"]).toBeDefined();
    expect(newState.byNames["ford"]).toBeDefined();
  });

  it("should remove user from state when delete succeeds", () => {
    const state = {
      list: {
        entries: ["ford", "zaphod"]
      },
      byNames: {
        zaphod: userZaphod,
        ford: userFord
      }
    };

    const newState = reducer(state, deleteUserSuccess(userFord));
    expect(newState.byNames["zaphod"]).toBeDefined();
    expect(newState.byNames["ford"]).toBeFalsy();
    expect(newState.list.entries).toEqual(["zaphod"]);
  });

  it("should set userCreatePermission to true if create link is present", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.list.entry.userCreatePermission).toBeTruthy();
    expect(newState.list.entries).toEqual(["zaphod", "ford"]);
    expect(newState.byNames["ford"]).toBeTruthy();
    expect(newState.byNames["zaphod"]).toBeTruthy();
  });

  it("should update state according to FETCH_USER_SUCCESS action", () => {
    const newState = reducer({}, fetchUserSuccess(userFord));
    expect(newState.byNames["ford"]).toBe(userFord);
  });

  it("should affect users state nor the state of other users", () => {
    const newState = reducer(
      {
        list: {
          entries: ["zaphod"]
        }
      },
      fetchUserSuccess(userFord)
    );
    expect(newState.byNames["ford"]).toBe(userFord);
    expect(newState.list.entries).toEqual(["zaphod"]);
  });

  it("should update state according to MODIFY_USER_SUCCESS action", () => {
    const newState = reducer(
      {
        byNames: {
          ford: {
            name: "ford"
          }
        }
      },
      modifyUserSuccess(userFord)
    );
    expect(newState.byNames["ford"]).toBe(userFord);
  });
});

describe("selector tests", () => {
  it("should return an empty object", () => {
    expect(selectListAsCollection({})).toEqual({});
    expect(selectListAsCollection({ users: { a: "a" } })).toEqual({});
  });

  it("should return a state slice collection", () => {
    const collection = {
      page: 3,
      totalPages: 42
    };

    const state = {
      users: {
        list: {
          entry: collection
        }
      }
    };
    expect(selectListAsCollection(state)).toBe(collection);
  });

  it("should return false", () => {
    expect(isPermittedToCreateUsers({})).toBe(false);
    expect(isPermittedToCreateUsers({ users: { list: { entry: {} } } })).toBe(
      false
    );
    expect(
      isPermittedToCreateUsers({
        users: { list: { entry: { userCreatePermission: false } } }
      })
    ).toBe(false);
  });

  it("should return true", () => {
    const state = {
      users: {
        list: {
          entry: {
            userCreatePermission: true
          }
        }
      }
    };
    expect(isPermittedToCreateUsers(state)).toBe(true);
  });
});
