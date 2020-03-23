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

import reducer, { isPending } from "./pending";

describe("pending reducer", () => {
  it("should set pending for FETCH_ITEMS to true", () => {
    const newState = reducer(
      {},
      {
        type: "FETCH_ITEMS_PENDING"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(true);
  });

  it("should do nothing for unknown action types", () => {
    const state = {};
    const newState = reducer(state, {
      type: "UNKNOWN"
    });
    expect(newState).toBe(state);
  });

  it("should set pending for FETCH_ITEMS to true, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: true
      },
      {
        type: "FETCH_ITEMS_PENDING"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(true);
    expect(newState["FETCH_USERS"]).toBe(true);
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_SUCCESS", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      {
        type: "FETCH_ITEMS_SUCCESS"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_FAILURE", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      {
        type: "FETCH_ITEMS_FAILURE"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_RESET", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      {
        type: "FETCH_ITEMS_RESET"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS, if resetPending prop is available", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: true
      },
      {
        type: "FETCH_ITEMS_SOMETHING",
        resetPending: true
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset pending state for FETCH_ITEMS after FETCH_ITEMS_SUCCESS, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: true,
        FETCH_ITEMS: true
      },
      {
        type: "FETCH_ITEMS_SUCCESS"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
    expect(newState["FETCH_USERS"]).toBe(true);
  });

  it("should set pending for a single item", () => {
    const newState = reducer(
      {
        "FETCH_USER/42": false
      },
      {
        type: "FETCH_USER_PENDING",
        itemId: 21
      }
    );
    expect(newState["FETCH_USER/21"]).toBe(true);
    expect(newState["FETCH_USER/42"]).toBe(false);
  });

  it("should reset pending for a single item", () => {
    const newState = reducer(
      {
        "FETCH_USER/42": true
      },
      {
        type: "FETCH_USER_SUCCESS",
        itemId: 42
      }
    );
    expect(newState["FETCH_USER/42"]).toBeFalsy();
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
