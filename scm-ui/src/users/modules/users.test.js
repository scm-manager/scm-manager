//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import {
  FETCH_USERS,
  FETCH_USERS_SUCCESS,
  fetchUsers,
  FETCH_USERS_FAILURE,
  addUser,
  ADD_USER,
  ADD_USER_SUCCESS,
  ADD_USER_FAILURE,
  updateUser,
  UPDATE_USER,
  UPDATE_USER_FAILURE,
  UPDATE_USER_SUCCESS,
  EDIT_USER,
  requestDeleteUser,
  deleteUserFailure,
  DELETE_USER,
  DELETE_USER_SUCCESS,
  DELETE_USER_FAILURE,
  deleteUser,
  updateUserFailure,
  deleteUserSuccess
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
  headers: {
    "content-type": "application/json"
  },
  responseBody
};

describe("users fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch users", () => {
    fetchMock.getOnce("/scm/api/rest/v2/users", response);

    const expectedActions = [{
        type: FETCH_USERS
      },
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
    fetchMock.getOnce("/scm/api/rest/v2/users", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUsers()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USERS);
      expect(actions[1].type).toEqual(FETCH_USERS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should add a user successfully", () => {
    // unmatched
    fetchMock.postOnce("/scm/api/rest/v2/users", {
      status: 204
    });

    // after create, the users are fetched again
    fetchMock.getOnce("/scm/api/rest/v2/users", response);

    const store = mockStore({});
    return store.dispatch(addUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(ADD_USER);
      expect(actions[1].type).toEqual(ADD_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USERS);
    });
  });

  it("should fail adding a user on HTTP 500", () => {
    fetchMock.postOnce("/scm/api/rest/v2/users", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(addUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(ADD_USER);
      expect(actions[1].type).toEqual(ADD_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("successfully update user", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });
    // after update, the users are fetched again
    fetchMock.getOnce("/scm/api/rest/v2/users", response);

    const store = mockStore({});
    return store.dispatch(updateUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(UPDATE_USER);
      expect(actions[1].type).toEqual(UPDATE_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USERS);
    });
  });

  it("should fail updating user on HTTP 500", () => {
    fetchMock.putOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(updateUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(UPDATE_USER);
      expect(actions[1].type).toEqual(UPDATE_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should delete successfully user zaphod", () => {
    fetchMock.deleteOnce("http://localhost:8081/scm/api/rest/v2/users/zaphod", {
      status: 204
    });
    // after update, the users are fetched again
    fetchMock.getOnce("/scm/api/rest/v2/users", response);

    const store = mockStore({});
    return store.dispatch(deleteUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(DELETE_USER);
      expect(actions[0].payload).toBe(userZaphod);
      expect(actions[1].type).toEqual(DELETE_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USERS);
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

  it("should update state correctly according to FETCH_USERS action", () => {
    const newState = reducer({}, {
      type: FETCH_USERS
    });
    expect(newState.users.loading).toBeTruthy();
    expect(newState.users.error).toBeFalsy();
  });

  it("should update state correctly according to FETCH_USERS_SUCCESS action", () => {
    const newState = reducer({}, {
      type: FETCH_USERS_SUCCESS,
      payload: responseBody
    });

    expect(newState.users).toEqual({
      entries: ["zaphod", "ford"],
      error: null,
      loading: false
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

    const newState = reducer(state, requestDeleteUser(userZaphod));
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

    const newState = reducer(state, requestDeleteUser(userZaphod));
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

  it("should delete deleted user in users in state if delete user action is successful", () => {
    const state = {
      users: {
        entries: [
          "zaphod",
          "ford"
        ]
      }
    };
    const newState = reducer(state, deleteUserSuccess(userZaphod));
    expect(newState.users.entries).toContain("ford");
    expect(newState.users.entries).not.toContain("zaphod");
  });

  it("should delete deleted user in usersByNames in state if delete user action is successful", () => {
    const state = {
      users: {
        entries: [
          "zaphod",
          "ford"
        ]
      },
      usersByNames: {
        zaphod: {
          entry: userZaphod
        },
        ford: {
          entry: userFord
        }
      }
    };
    const newState = reducer(state, deleteUserSuccess(userZaphod));
    const ford = newState.usersByNames["ford"];
    const zaphod = newState.usersByNames["zaphod"];
    expect(zaphod).toBeUndefined();
    expect(ford.entry).toBe(userFord);
  });

  it("should set global error state if one user could not be deleted", () => {
    const state = {
      users: {
        error: null
      },
      usersByNames: {
        zaphod: {
          loading: false,
          error: null,
          entry: userZaphod
        }
      }
    };

    const error = new Error("could not delete user zaphod: ..");
    const newState = reducer(state, deleteUserFailure(userZaphod, error));
    expect(newState.users.error).toBe(error);
  });

  it("should not set global error state if one user could not be edited", () => {
    const state = {
      users: {
        error: null
      },
      usersByNames: {
        zaphod: {
          loading: false,
          error: null,
          entry: userZaphod
        }
      }
    };

    const error = new Error("could not edit user zaphod: ..");
    const newState = reducer(state, updateUserFailure(userZaphod, error));
    expect(newState.users.error).toBe(null);
  });

  it("should not replace whole usersByNames map when fetching users", () => {
    const oldState = {
      usersByNames: {
        ford: {
          entry: userFord
        }
      }
    };

    const newState = reducer(oldState, {
      type: FETCH_USERS_SUCCESS,
      payload: responseBodyZaphod
    });
    expect(newState.usersByNames["zaphod"]).toBeDefined();
    expect(newState.usersByNames["ford"]).toBeDefined();
  });

  it("should update state correctly according to EDIT_USER action", () => {
    const newState = reducer({}, {
      type: EDIT_USER,
      user: userZaphod
    });
    expect(newState.editUser).toEqual(userZaphod);
  });
});
