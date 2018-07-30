// @flow
import reducer, { getFailure } from "./failure";

const err = new Error("something failed");
const otherErr = new Error("something else failed");

describe("failure reducer", () => {
  it("should set the error for FETCH_ITEMS", () => {
    const newState = reducer({}, { type: "FETCH_ITEMS_FAILURE", payload: err });
    expect(newState["FETCH_ITEMS"]).toBe(err);
  });

  it("should do nothing for unknown action types", () => {
    const state = {};
    const newState = reducer(state, { type: "UNKNOWN" });
    expect(newState).toBe(state);
  });

  it("should set the error for FETCH_ITEMS, if payload has multiple values", () => {
    const newState = reducer(
      {},
      {
        type: "FETCH_ITEMS_FAILURE",
        payload: { something: "something", error: err }
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(err);
  });

  it("should set the error for FETCH_ITEMS, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: otherErr
      },
      { type: "FETCH_ITEMS_FAILURE", payload: err }
    );
    expect(newState["FETCH_ITEMS"]).toBe(err);
    expect(newState["FETCH_USERS"]).toBe(otherErr);
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_SUCCESS", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err
      },
      { type: "FETCH_ITEMS_SUCCESS" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_RESET", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err
      },
      { type: "FETCH_ITEMS_RESET" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_RESET, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err,
        FETCH_USERS: err
      },
      { type: "FETCH_ITEMS_RESET" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
    expect(newState["FETCH_USERS"]).toBe(err);
  });

  it("should set the error for a single item of FETCH_ITEM", () => {
    const newState = reducer(
      {},
      { type: "FETCH_ITEM_FAILURE", payload: err, itemId: 42 }
    );
    expect(newState["FETCH_ITEM/42"]).toBe(err);
  });

  it("should reset error for a single item of FETCH_ITEM", () => {
    const newState = reducer(
      { "FETCH_ITEM/42": err },
      { type: "FETCH_ITEM_SUCCESS", payload: err, itemId: 42 }
    );
    expect(newState["FETCH_ITEM/42"]).toBeUndefined();
  });
});

describe("failure selector", () => {
  it("should return failure, if FETCH_ITEMS failure exists", () => {
    const failure = getFailure(
      {
        failure: {
          FETCH_ITEMS: err
        }
      },
      "FETCH_ITEMS"
    );
    expect(failure).toBe(err);
  });

  it("should return undefined, if state has no failure", () => {
    const failure = getFailure({}, "FETCH_ITEMS");
    expect(failure).toBeUndefined();
  });

  it("should return undefined, if FETCH_ITEMS is not defined", () => {
    const failure = getFailure(
      {
        failure: {}
      },
      "FETCH_ITEMS"
    );
    expect(failure).toBeFalsy();
  });

  it("should return failure, if FETCH_ITEM 42 failure exists", () => {
    const failure = getFailure(
      {
        failure: {
          "FETCH_ITEM/42": err
        }
      },
      "FETCH_ITEM",
      42
    );
    expect(failure).toBe(err);
  });
});
