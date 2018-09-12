import configureMockStore from "redux-mock-store";
import thunk from "redux-thunk";
import fetchMock from "fetch-mock";
import {
  FETCH_BRANCHES_FAILURE,
  FETCH_BRANCHES_PENDING,
  FETCH_BRANCHES_SUCCESS,
  fetchBranchesByNamespaceAndName
} from "./branches";

describe("fetch branches", () => {
  const URL = "/api/rest/v2/repositories/foo/bar/branches";
  const mockStore = configureMockStore([thunk]);

  afterEach(() => {
    fetchMock.reset();
    fetchMock.restore();
  });


  it("should fetch branches", () => {
    const collection = {};

    fetchMock.getOnce(URL, "{}");

    const expectedActions = [
      {type: FETCH_BRANCHES_PENDING, payload: {namespace: "foo", name: "bar"},
        itemId: "foo/bar"},
      {
        type: FETCH_BRANCHES_SUCCESS,
        payload: {data: collection, namespace: "foo", name: "bar"},
        itemId: "foo/bar"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchBranchesByNamespaceAndName("foo", "bar")).then(() => {
      expect(store.getActions()).toEqual(expectedActions);
    });
  });

  it("should fail fetching branches on HTTP 500", () => {
    const collection = {};

    fetchMock.getOnce(URL, 500);

    const expectedActions = [
      {type: FETCH_BRANCHES_PENDING, payload: {namespace: "foo", name: "bar"},
        itemId: "foo/bar"},
      {
        type: FETCH_BRANCHES_FAILURE,
        payload: {error: collection, namespace: "foo", name: "bar"},
        itemId: "foo/bar"
      }
    ];

    const store = mockStore({});
    return store.dispatch(fetchBranchesByNamespaceAndName("foo", "bar")).then(() => {
      expect(store.getActions()[0]).toEqual(expectedActions[0]);
      expect(store.getActions()[1].type).toEqual(FETCH_BRANCHES_FAILURE);
    });
  })
});
