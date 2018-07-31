//@flow
import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";

import reducer, {
  fetchGroups,
  FETCH_GROUPS_PENDING,
  FETCH_GROUPS_SUCCESS,
  FETCH_GROUPS_FAILURE,
  fetchGroupsSuccess
} from "./groups"
const GROUPS_URL = "/scm/api/rest/v2/groups";

const groupZaphod = {
  creationDate: "2018-07-31T08:39:07.860Z",
  description: "This is a group",
  name: "zaphodGroup",
  type: "xml",
  properties: {},
  members: ["userZaphod"],
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/zaphodGroup"
    },
    delete: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/zaphodGroup"
    },
    update: {
      href:"http://localhost:3000/scm/api/rest/v2/groups/zaphodGroup"
    }
  },
  _embedded: {
    members: [{
      name:"userZaphod",
      _links: {
        self :{
          href: "http://localhost:3000/scm/api/rest/v2/users/userZaphod"
        }
      }
    }]
  }
};

const groupFord = {
  creationDate: "2018-07-31T08:39:07.860Z",
  description: "This is a group",
  name: "fordGroup",
  type: "xml",
  properties: {},
  members: [],
  _links: {
    self: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/fordGroup"
    },
    delete: {
      href: "http://localhost:3000/scm/api/rest/v2/groups/fordGroup"
    },
    update: {
      href:"http://localhost:3000/scm/api/rest/v2/groups/fordGroup"
    }
  },
  _embedded: {
    members: []
  }
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

describe("groups reducer", () => {

  it("should update state correctly according to FETCH_USERS_SUCCESS action", () => {
    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list).toEqual({
      entries: ["zaphodGroup", "fordGroup"],
      entry: {
        groupCreatePermission: true,
        page: 0,
        pageTotal: 1,
        _links: responseBody._links
      }
    });

    expect(newState.byNames).toEqual({
      zaphodGroup: groupZaphod,
      fordGroup: groupFord
    });

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
  });

  it("should set groupCreatePermission to true if update link is present", () => {
    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
  });

  it("should not replace whole byNames map when fetching users", () => {
    const oldState = {
      byNames: {
        fordGroup: groupFord
      }
    };

    const newState = reducer(oldState, fetchGroupsSuccess(responseBody));
    expect(newState.byNames["zaphodGroup"]).toBeDefined();
    expect(newState.byNames["fordGroup"]).toBeDefined();
  });

  it("should set userCreatePermission to true if create link is present", () => {
    const newState = reducer({}, fetchGroupsSuccess(responseBody));

    expect(newState.list.entry.groupCreatePermission).toBeTruthy();
    expect(newState.list.entries).toEqual(["zaphodGroup", "fordGroup"]);
    expect(newState.byNames["fordGroup"]).toBeTruthy();
    expect(newState.byNames["zaphodGroup"]).toBeTruthy();
  });
});
