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
  modifyUserPending,
  modifyUserSuccess,
  modifyUserFailure,
  fetchUserSuccess,
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
    expect(newState.list.loading).toBeTruthy();
    expect(newState.list.error).toBeFalsy();
  });

  it("should update state correctly according to FETCH_USERS_SUCCESS action", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.list).toEqual({
      entries: ["zaphod", "ford"],
      error: null,
      loading: false,
      userCreatePermission: true
    });

    expect(newState.byNames).toEqual({
      zaphod: {
        entry: userZaphod
      },
      ford: {
        entry: userFord
      }
    });

    expect(newState.list.userCreatePermission).toBeTruthy();
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
    const zaphod = newState.byNames["zaphod"];
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
    const zaphod = newState.byNames["zaphod"];
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

  it("should not replace whole byNames map when fetching users", () => {
    const oldState = {
      byNames: {
        ford: {
          entry: userFord
        }
      }
    };

    const newState = reducer(oldState, fetchUsersSuccess(responseBody));
    expect(newState.byNames["zaphod"]).toBeDefined();
    expect(newState.byNames["ford"]).toBeDefined();
  });

  it("should set userCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchUsersSuccess(responseBody));

    expect(newState.list.userCreatePermission).toBeTruthy();
  });

  it("should update state correctly according to CREATE_USER_PENDING action", () => {
    const newState = reducer({}, createUserPending(userZaphod));
    expect(newState.create.loading).toBeTruthy();
    expect(newState.create.error).toBeFalsy();
  });

  it("should update state correctly according to CREATE_USER_SUCCESS action", () => {
    const newState = reducer({ loading: true }, createUserSuccess());
    expect(newState.create.loading).toBeFalsy();
    expect(newState.create.error).toBeFalsy();
  });

  it("should set the loading to false and the error if user could not be created", () => {
    const newState = reducer(
      { loading: true, error: null },
      createUserFailure(userFord, new Error("kaputt kaputt"))
    );
    expect(newState.create.loading).toBeFalsy();
    expect(newState.create.error).toEqual(new Error("kaputt kaputt"));
  });

  it("should update state according to FETCH_USER_PENDING action", () => {
    const newState = reducer({}, fetchUserPending("zaphod"));
    expect(newState.byNames["zaphod"].loading).toBeTruthy();
  });

  it("should not affect users state", () => {
    const newState = reducer(
      {
        users: {
          entries: ["ford"]
        }
      },
      fetchUserPending("zaphod")
    );
    expect(newState.byNames["zaphod"].loading).toBeTruthy();
    expect(newState.users.entries).toEqual(["ford"]);
  });

  it("should update state according to FETCH_USER_FAILURE action", () => {
    const error = new Error("kaputt!");
    const newState = reducer({}, fetchUserFailure(userFord.name, error));
    expect(newState.byNames["ford"].error).toBe(error);
    expect(newState.byNames["ford"].loading).toBeFalsy();
  });

  it("should update state according to FETCH_USER_SUCCESS action", () => {
    const newState = reducer({}, fetchUserSuccess(userFord));
    expect(newState.byNames["ford"].loading).toBeFalsy();
    expect(newState.byNames["ford"].entry).toBe(userFord);
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
    expect(newState.byNames["ford"].loading).toBeFalsy();
    expect(newState.byNames["ford"].entry).toBe(userFord);
    expect(newState.list.entries).toEqual(["zaphod"]);
  });

  it("should update state according to MODIFY_USER_PENDING action", () => {
    const newState = reducer(
      {
        error: new Error("something"),
        entry: {}
      },
      modifyUserPending(userFord)
    );
    expect(newState.byNames["ford"].loading).toBeTruthy();
    expect(newState.byNames["ford"].error).toBeFalsy();
    expect(newState.byNames["ford"].entry).toBeFalsy();
  });

  it("should update state according to MODIFY_USER_SUCCESS action", () => {
    const newState = reducer(
      {
        loading: true,
        error: new Error("something"),
        entry: {}
      },
      modifyUserSuccess(userFord)
    );
    expect(newState.byNames["ford"].loading).toBeFalsy();
    expect(newState.byNames["ford"].error).toBeFalsy();
    expect(newState.byNames["ford"].entry).toBe(userFord);
  });

  it("should update state according to MODIFY_USER_SUCCESS action", () => {
    const error = new Error("something went wrong");
    const newState = reducer(
      {
        loading: true,
        entry: {}
      },
      modifyUserFailure(userFord, error)
    );
    expect(newState.byNames["ford"].loading).toBeFalsy();
    expect(newState.byNames["ford"].error).toBe(error);
    expect(newState.byNames["ford"].entry).toBeFalsy();
  });
});
