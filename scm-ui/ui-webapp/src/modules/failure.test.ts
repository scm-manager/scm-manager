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
///
/// MIT License
///
/// Copyright (c) 2020-present Cloudogu GmbH and Contributors
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in all
/// copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
/// SOFTWARE.
///

import reducer, { getFailure } from "./failure";

const err = new Error("something failed");
const otherErr = new Error("something else failed");

describe("failure reducer", () => {
  it("should set the error for FETCH_ITEMS", () => {
    const newState = reducer(
      {},
      {
        type: "FETCH_ITEMS_FAILURE",
        payload: err
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(err);
  });

  it("should do nothing for unknown action types", () => {
    const state = {};
    const newState = reducer(state, {
      type: "UNKNOWN"
    });
    expect(newState).toBe(state);
  });

  it("should set the error for FETCH_ITEMS, if payload has multiple values", () => {
    const newState = reducer(
      {},
      {
        type: "FETCH_ITEMS_FAILURE",
        payload: {
          something: "something",
          error: err
        }
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(err);
  });

  it("should set the error for FETCH_ITEMS, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_USERS: otherErr
      },
      {
        type: "FETCH_ITEMS_FAILURE",
        payload: err
      }
    );
    expect(newState["FETCH_ITEMS"]).toBe(err);
    expect(newState["FETCH_USERS"]).toBe(otherErr);
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_SUCCESS", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err
      },
      {
        type: "FETCH_ITEMS_SUCCESS"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_RESET", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err
      },
      {
        type: "FETCH_ITEMS_RESET"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
  });

  it("should reset FETCH_ITEMS after FETCH_ITEMS_RESET, but should not affect others", () => {
    const newState = reducer(
      {
        FETCH_ITEMS: err,
        FETCH_USERS: err
      },
      {
        type: "FETCH_ITEMS_RESET"
      }
    );
    expect(newState["FETCH_ITEMS"]).toBeFalsy();
    expect(newState["FETCH_USERS"]).toBe(err);
  });

  it("should set the error for a single item of FETCH_ITEM", () => {
    const newState = reducer(
      {},
      {
        type: "FETCH_ITEM_FAILURE",
        payload: err,
        itemId: 42
      }
    );
    expect(newState["FETCH_ITEM/42"]).toBe(err);
  });

  it("should reset error for a single item of FETCH_ITEM", () => {
    const newState = reducer(
      {
        "FETCH_ITEM/42": err
      },
      {
        type: "FETCH_ITEM_SUCCESS",
        payload: err,
        itemId: 42
      }
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
