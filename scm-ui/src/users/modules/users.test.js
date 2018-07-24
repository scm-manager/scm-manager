//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import {
  FETCH_USERS_PENDING,
  FETCH_USERS_SUCCESS,
  fetchUsers,
  FETCH_USERS_FAILURE,
  createUserPending,
  CREATE_USER_PENDING,
  CREATE_USER_SUCCESS,
  CREATE_USER_FAILURE,
  modifyUser,
  MODIFY_USER_PENDING,
  MODIFY_USER_FAILURE,
  MODIFY_USER_SUCCESS,
  deleteUserPending,
  deleteUserFailure,
  DELETE_USER,
  DELETE_USER_SUCCESS,
  DELETE_USER_FAILURE,
  deleteUser,
  fetchUsersFailure,
  fetchUsersSuccess,
  fetchUser,
  FETCH_USER_PENDING,
  FETCH_USER_SUCCESS,
  FETCH_USER_FAILURE,
  createUser,
  createUserSuccess,
  createUserFailure,
  modifyUserFailure,
  deleteUserSuccess,
  fetchUsersPending,
  fetchUserPending,
  fetchUserFailure
} from "./users";

import reducer from "./users";

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

const responseBodyZaphod = {
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
    users: [userZaphod]
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
      expect(actions[2].type).toEqual(FETCH_USERS_PENDING);
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

  it("successfully update user", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });
    // after update, the users are fetched again
    fetchMock.getOnce(USERS_URL, response);

    const store = mockStore({});
    return store.dispatch(modifyUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(MODIFY_USER_PENDING);
      expect(actions[1].type).toEqual(MODIFY_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USERS_PENDING);
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
    // after update, the users are fetched again
    fetchMock.getOnce(USERS_URL, response);

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_USER);
      expect(actions[0].payload).toBe(userZaphod);
      expect(actions[1].type).toEqual(DELETE_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USERS_PENDING);
    });
  });

  it("should fail to delete user zaphod", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_USER);
      expect(actions[0].payload).toBe(userZaphod);
      expect(actions[1].type).toEqual(DELETE_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});

describe("users reducer", () => {
  it("should update state correctly according to FETCH_USERS_PENDING action", () => {
    const newState = reducer({}, fetchUsersPending());
    expect(newState.users.loading).toBeTruthy();
    expect(newState.users.error).toBeFalsy();
  });

  it("should update state correctly according to FETCH_USERS_SUCCESS action", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.users).toEqual({
      entries: ["zaphod", "ford"],
      error: null,
      loading: false,
      userCreatePermission: true
    });

    expect(newState.usersByNames).toEqual({
      zaphod: {
        entry: userZaphod
      },
      ford: {
        entry: userFord
      }
    });
  });

  test("should update state correctly according to DELETE_USER action", () => {
    const state = {
      usersByNames: {
        zaphod: {
          loading: false,
          error: null,
          entry: userZaphod
        }
      }
    };

    const newState = reducer(state, deleteUserPending(userZaphod));
    const zaphod = newState.usersByNames["zaphod"];
    expect(zaphod.loading).toBeTruthy();
    expect(zaphod.entry).toBe(userZaphod);
  });

  it("should not effect other users if one user will be deleted", () => {
    const state = {
      usersByNames: {
        zaphod: {
          loading: false,
          error: null,
          entry: userZaphod
        },
        ford: {
          loading: false
        }
      }
    };

    const newState = reducer(state, deleteUserPending(userZaphod));
    const ford = newState.usersByNames["ford"];
    expect(ford.loading).toBeFalsy();
  });

  it("should set the error of user which could not be deleted", () => {
    const state = {
      usersByNames: {
        zaphod: {
          loading: true,
          entry: userZaphod
        }
      }
    };

    const error = new Error("error");
    const newState = reducer(state, deleteUserFailure(userZaphod, error));
    const zaphod = newState.usersByNames["zaphod"];
    expect(zaphod.loading).toBeFalsy();
    expect(zaphod.error).toBe(error);
  });

  it("should not effect other users if one user could not be deleted", () => {
    const state = {
      usersByNames: {
        zaphod: {
          loading: false,
          error: null,
          entry: userZaphod
        },
        ford: {
          loading: false
        }
      }
    };

    const error = new Error("error");
    const newState = reducer(state, deleteUserFailure(userZaphod, error));
    const ford = newState.usersByNames["ford"];
    expect(ford.loading).toBeFalsy();
  });

  it("should not replace whole usersByNames map when fetching users", () => {
    const oldState = {
      usersByNames: {
        ford: {
          entry: userFord
        }
      }
    };

    const newState = reducer(oldState, fetchUsersSuccess(responseBody));
    expect(newState.usersByNames["zaphod"]).toBeDefined();
    expect(newState.usersByNames["ford"]).toBeDefined();
  });

  it("should set userCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.users.userCreatePermission).toBeTruthy();
  });

  it("should update state correctly according to CREATE_USER_PENDING action", () => {
    const newState = reducer({}, createUserPending(userZaphod));
    expect(newState.users.loading).toBeTruthy();
    expect(newState.users.error).toBeNull();
  });

  it("should update state correctly according to CREATE_USER_SUCCESS action", () => {
    const newState = reducer({ loading: true }, createUserSuccess());
    expect(newState.users.loading).toBeFalsy();
    expect(newState.users.error).toBeNull();
  });

  it("should set the loading to false and the error if user could not be added", () => {
    const newState = reducer(
      { loading: true, error: null },
      createUserFailure(userFord, new Error("kaputt kaputt"))
    );
    expect(newState.users.loading).toBeFalsy();
    expect(newState.users.error).toEqual(new Error("kaputt kaputt"));
  });

  it("should update state according to FETCH_USER_PENDING action", () => {
    const newState = reducer({}, fetchUserPending("zaphod"));
    expect(newState.usersByNames["zaphod"].loading).toBeTruthy();
  });

  it("should update state according to FETCH_USER_FAILURE action", () => {
    const newState = reducer(
      {},
      fetchUserFailure(userFord.name, new Error("kaputt!"))
    );
    expect(newState.usersByNames["ford"].error).toBeTruthy;
  });
});
