/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  CREATE_USER,
  CREATE_USER_FAILURE,
  CREATE_USER_PENDING,
  CREATE_USER_SUCCESS,
  createUser,
  DELETE_USER,
  DELETE_USER_FAILURE,
  DELETE_USER_PENDING,
  DELETE_USER_SUCCESS,
  deleteUser,
  deleteUserSuccess,
  FETCH_USER,
  FETCH_USER_FAILURE,
  FETCH_USER_PENDING,
  FETCH_USER_SUCCESS,
  FETCH_USERS,
  FETCH_USERS_FAILURE,
  FETCH_USERS_PENDING,
  FETCH_USERS_SUCCESS,
  fetchUserByLink,
  fetchUserByName,
  fetchUsers,
  fetchUsersSuccess,
  fetchUserSuccess,
  getCreateUserFailure,
  getDeleteUserFailure,
  getFetchUserFailure,
  getFetchUsersFailure,
  getModifyUserFailure,
  getUserByName,
  getUsersFromState,
  isCreateUserPending,
  isDeleteUserPending,
  isFetchUserPending,
  isFetchUsersPending,
  isModifyUserPending,
  isPermittedToCreateUsers,
  MODIFY_USER,
  MODIFY_USER_FAILURE,
  MODIFY_USER_PENDING,
  MODIFY_USER_SUCCESS,
  modifyUser,
  selectListAsCollection
} from "./users";

const userZaphod = {
  active: true,
  admin: true,
  creationDate: "2018-07-11T12:23:49.027Z",
  displayName: "Z. Beeblebrox",
  mail: "president@heartofgold.universe",
  name: "zaphod",
  password: "",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/users/zaphod"
    },
    delete: {
      href: "http://localhost:8081/api/v2/users/zaphod"
    },
    update: {
      href: "http://localhost:8081/api/v2/users/zaphod"
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
  password: "",
  type: "xml",
  properties: {},
  _links: {
    self: {
      href: "http://localhost:8081/api/v2/users/ford"
    },
    delete: {
      href: "http://localhost:8081/api/v2/users/ford"
    },
    update: {
      href: "http://localhost:8081/api/v2/users/ford"
    }
  }
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/api/v2/users/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/api/v2/users/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/api/v2/users/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/api/v2/users/"
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

const URL = "users";
const USERS_URL = "/api/v2/users";
const USER_ZAPHOD_URL = "http://localhost:8081/api/v2/users/zaphod";

const error = new Error("KAPUTT");

describe("users fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch users", () => {
    fetchMock.getOnce(USERS_URL, response);

    const expectedActions = [
      {
        type: FETCH_USERS_PENDING
      },
      {
        type: FETCH_USERS_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchUsers(URL)).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting users on HTTP 500", () => {
    fetchMock.getOnce(USERS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUsers(URL)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USERS_PENDING);
      expect(actions[1].type).toEqual(FETCH_USERS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single user by name", () => {
    fetchMock.getOnce(USERS_URL + "/zaphod", userZaphod);

    const store = mockStore({});
    return store.dispatch(fetchUserByName(URL, "zaphod")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USER_PENDING);
      expect(actions[1].type).toEqual(FETCH_USER_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single user by name on HTTP 500", () => {
    fetchMock.getOnce(USERS_URL + "/zaphod", {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUserByName(URL, "zaphod")).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USER_PENDING);
      expect(actions[1].type).toEqual(FETCH_USER_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should sucessfully fetch single user", () => {
    fetchMock.getOnce(USER_ZAPHOD_URL, userZaphod);

    const store = mockStore({});
    return store.dispatch(fetchUserByLink(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_USER_PENDING);
      expect(actions[1].type).toEqual(FETCH_USER_SUCCESS);
      expect(actions[1].payload).toBeDefined();
    });
  });

  it("should fail fetching single user on HTTP 500", () => {
    fetchMock.getOnce(USER_ZAPHOD_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchUserByLink(userZaphod)).then(() => {
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
    return store.dispatch(createUser(URL, userZaphod)).then(() => {
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
    return store.dispatch(createUser(URL, userZaphod)).then(() => {
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
    return store.dispatch(createUser(URL, userZaphod, callback)).then(() => {
      expect(callMe).toBe("yeah");
    });
  });

  it("successfully update user", () => {
    fetchMock.putOnce(USER_ZAPHOD_URL, {
      status: 204
    });
    fetchMock.getOnce(USER_ZAPHOD_URL, userZaphod);

    const store = mockStore({});
    return store.dispatch(modifyUser(userZaphod)).then(() => {
      const actions = store.getActions();
      expect(actions.length).toBe(3);
      expect(actions[0].type).toEqual(MODIFY_USER_PENDING);
      expect(actions[1].type).toEqual(MODIFY_USER_SUCCESS);
      expect(actions[2].type).toEqual(FETCH_USER_PENDING);
    });
  });

  it("should call callback, after successful modified user", () => {
    fetchMock.putOnce(USER_ZAPHOD_URL, {
      status: 204
    });
    fetchMock.getOnce(USER_ZAPHOD_URL, userZaphod);

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
    fetchMock.putOnce(USER_ZAPHOD_URL, {
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
    fetchMock.deleteOnce(USER_ZAPHOD_URL, {
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
    fetchMock.deleteOnce(USER_ZAPHOD_URL, {
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
    fetchMock.deleteOnce(USER_ZAPHOD_URL, {
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
});

describe("selector tests", () => {
  it("should return an empty object", () => {
    expect(selectListAsCollection({})).toEqual({});
    expect(
      selectListAsCollection({
        users: {
          a: "a"
        }
      })
    ).toEqual({});
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
    expect(
      isPermittedToCreateUsers({
        users: {
          list: {
            entry: {}
          }
        }
      })
    ).toBe(false);
    expect(
      isPermittedToCreateUsers({
        users: {
          list: {
            entry: {
              userCreatePermission: false
            }
          }
        }
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

  it("should get users from state", () => {
    const state = {
      users: {
        list: {
          entries: ["a", "b"]
        },
        byNames: {
          a: {
            name: "a"
          },
          b: {
            name: "b"
          }
        }
      }
    };
    expect(getUsersFromState(state)).toEqual([
      {
        name: "a"
      },
      {
        name: "b"
      }
    ]);
  });

  it("should return true, when fetch users is pending", () => {
    const state = {
      pending: {
        [FETCH_USERS]: true
      }
    };
    expect(isFetchUsersPending(state)).toEqual(true);
  });

  it("should return false, when fetch users is not pending", () => {
    expect(isFetchUsersPending({})).toEqual(false);
  });

  it("should return error when fetch users did fail", () => {
    const state = {
      failure: {
        [FETCH_USERS]: error
      }
    };
    expect(getFetchUsersFailure(state)).toEqual(error);
  });

  it("should return undefined when fetch users did not fail", () => {
    expect(getFetchUsersFailure({})).toBe(undefined);
  });

  it("should return true if create user is pending", () => {
    const state = {
      pending: {
        [CREATE_USER]: true
      }
    };
    expect(isCreateUserPending(state)).toBe(true);
  });

  it("should return false if create user is not pending", () => {
    const state = {
      pending: {
        [CREATE_USER]: false
      }
    };
    expect(isCreateUserPending(state)).toBe(false);
  });

  it("should return error when create user did fail", () => {
    const state = {
      failure: {
        [CREATE_USER]: error
      }
    };
    expect(getCreateUserFailure(state)).toEqual(error);
  });

  it("should return undefined when create user did not fail", () => {
    expect(getCreateUserFailure({})).toBe(undefined);
  });

  it("should return user ford", () => {
    const state = {
      users: {
        byNames: {
          ford: userFord
        }
      }
    };
    expect(getUserByName(state, "ford")).toEqual(userFord);
  });

  it("should return true, when fetch user zaphod is pending", () => {
    const state = {
      pending: {
        [FETCH_USER + "/zaphod"]: true
      }
    };
    expect(isFetchUserPending(state, "zaphod")).toEqual(true);
  });

  it("should return false, when fetch user zaphod is not pending", () => {
    expect(isFetchUserPending({}, "zaphod")).toEqual(false);
  });

  it("should return error when fetch user zaphod did fail", () => {
    const state = {
      failure: {
        [FETCH_USER + "/zaphod"]: error
      }
    };
    expect(getFetchUserFailure(state, "zaphod")).toEqual(error);
  });

  it("should return undefined when fetch user zaphod did not fail", () => {
    expect(getFetchUserFailure({}, "zaphod")).toBe(undefined);
  });

  it("should return true, when modify user ford is pending", () => {
    const state = {
      pending: {
        [MODIFY_USER + "/ford"]: true
      }
    };
    expect(isModifyUserPending(state, "ford")).toEqual(true);
  });

  it("should return false, when modify user ford is not pending", () => {
    expect(isModifyUserPending({}, "ford")).toEqual(false);
  });

  it("should return error when modify user ford did fail", () => {
    const state = {
      failure: {
        [MODIFY_USER + "/ford"]: error
      }
    };
    expect(getModifyUserFailure(state, "ford")).toEqual(error);
  });

  it("should return undefined when modify user ford did not fail", () => {
    expect(getModifyUserFailure({}, "ford")).toBe(undefined);
  });

  it("should return true, when delete user zaphod is pending", () => {
    const state = {
      pending: {
        [DELETE_USER + "/zaphod"]: true
      }
    };
    expect(isDeleteUserPending(state, "zaphod")).toEqual(true);
  });

  it("should return false, when delete user zaphod is not pending", () => {
    expect(isDeleteUserPending({}, "zaphod")).toEqual(false);
  });

  it("should return error when delete user zaphod did fail", () => {
    const state = {
      failure: {
        [DELETE_USER + "/zaphod"]: error
      }
    };
    expect(getDeleteUserFailure(state, "zaphod")).toEqual(error);
  });

  it("should return undefined when delete user zaphod did not fail", () => {
    expect(getDeleteUserFailure({}, "zaphod")).toBe(undefined);
  });
});
