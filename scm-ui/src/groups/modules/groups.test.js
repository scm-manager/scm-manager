//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import {fetchGroups,
  FETCH_GROUPS_PENDING,
  FETCH_GROUPS_SUCCESS,
  FETCH_GROUPS_FAILURE
} from "./groups"
const GROUPS_URL = "/scm/api/rest/v2/groups";

const groupZaphod = {
};

const groupFord = {
};

const responseBody = {
  page: 0,
  pageTotal: 1,
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    first: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    last: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/?page=0&pageSize=10"
    },
    create: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/"
    }
  },
  _embedded: {
    groups: [groupZaphod, groupFord]
  }
};

const response = {
  headers: { "content-type": "application/json" },
  responseBody
};


const error = new Error("KAPUTT");

describe("groups fetch()", () => {
  const mockStore = configureMockStore([thunk]);
  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });

  it("should successfully fetch groups", () => {
    fetchMock.getOnce(GROUPS_URL, response);

    const expectedActions = [
      { type: FETCH_GROUPS_PENDING },
      {
        type: FETCH_GROUPS_SUCCESS,
        payload: response
      }
    ];

    const store = mockStore({});

    return store.dispatch(fetchGroups()).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail getting groups on HTTP 500", () => {
    fetchMock.getOnce(GROUPS_URL, {
      status: 500
    });

    const store = mockStore({});
    return store.dispatch(fetchGroups()).then(() => {
      const actions = store.getActions();
      expect(actions[0].type).toEqual(FETCH_GROUPS_PENDING);
      expect(actions[1].type).toEqual(FETCH_GROUPS_FAILURE);
      expect(actions[1].payload).toBeDefined();
    });
  });
});
