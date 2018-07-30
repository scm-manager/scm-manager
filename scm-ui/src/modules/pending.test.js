import reducer, { isPending } from "./pending";

describe("pending reducer", () => {
  it("should set pending for FETCH_ITEMS to true", () => {
    const newState = reducer({}, { type: "FETCH_ITEMS_PENDING" });
    expect(newState["FETCH_ITEMS"]).toBe(true);
  });

  it("should set pending for FETCH_ITEMS to true, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: true
      },
      { type: "FETCH_ITEMS_PENDING" }
    );
    expect(newState["FETCH_ITEMS"]).toBe(true);
    expect(newState["FETCH_USERS"]).toBe(true);
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_SUCCESS", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      { type: "FETCH_ITEMS_SUCCESS" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_FAILURE", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      { type: "FETCH_ITEMS_FAILURE" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_RESET", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      { type: "FETCH_ITEMS_RESET" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_SUCCESS, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: true,
        FETCH_ITEMS: true
      },
      { type: "FETCH_ITEMS_SUCCESS" }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
    expect(newState["FETCH_USERS"]).toBe(true);
  });

  it("should set pending for a single item", () => {
    const newState = reducer(
      {
        "FETCH_USER/42": false
      },
      { type: "FETCH_USER_PENDING", itemId: 21 }
    );
    expect(newState["FETCH_USER/21"]).toBe(true);
    expect(newState["FETCH_USER/42"]).toBe(false);
  });
});

describe("pending selectors", () => {
  it("should return true, while FETCH_ITEMS is pending", () => {
    const result = isPending(
      {
        pending: {
          FETCH_ITEMS: true
        }
      },
      "FETCH_ITEMS"
    );
    expect(result).toBe(true);
  });

  it("should return false, if pending is not defined", () => {
    const result = isPending({}, "FETCH_ITEMS");
    expect(result).toBe(false);
  });

  it("should return true, while FETCH_ITEM 42 is pending", () => {
    const result = isPending(
      {
        pending: {
          "FETCH_ITEM/42": true
        }
      },
      "FETCH_ITEM",
      42
    );
    expect(result).toBe(true);
  });

  it("should return true, while FETCH_ITEM 42 is undefined", () => {
    const result = isPending(
      {
        pending: {
          "FETCH_ITEM/21": true
        }
      },
      "FETCH_ITEM",
      42
    );
    expect(result).toBe(false);
  });
});
