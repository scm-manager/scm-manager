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

import { Action } from "@scm-manager/ui-types";

const FAILURE_SUFFIX = "_FAILURE";
const RESET_PATTERN = /^(.*)_(SUCCESS|RESET)$/;

function extractIdentifierFromFailure(action: Action) {
  const type = action.type;
  let identifier = type.substring(0, type.length - FAILURE_SUFFIX.length);
  if (action.itemId) {
    identifier += "/" + action.itemId;
  }
  return identifier;
}

function removeAllEntriesOfIdentifierFromState(state: object, payload: any, identifier: string) {
  const newState = {};
  for (const failureType in state) {
    if (failureType !== identifier && !failureType.startsWith(identifier)) {
      // @ts-ignore Right types not available
      newState[failureType] = state[failureType];
    }
  }
  return newState;
}

function removeFromState(state: object, identifier: string) {
  const newState = {};
  for (const failureType in state) {
    if (failureType !== identifier) {
      // @ts-ignore Right types not available
      newState[failureType] = state[failureType];
    }
  }
  return newState;
}

export default function reducer(
  state: object = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  const type = action.type;
  if (type.endsWith(FAILURE_SUFFIX)) {
    const identifier = extractIdentifierFromFailure(action);
    let payload;
    if (action.payload instanceof Error) {
      payload = action.payload;
    } else if (action.payload) {
      payload = action.payload.error;
    }
    return {
      ...state,
      [identifier]: payload
    };
  } else {
    const match = RESET_PATTERN.exec(type);
    if (match) {
      let identifier = match[1];
      if (action.itemId) {
        identifier += "/" + action.itemId;
      }
      if (action.payload) return removeAllEntriesOfIdentifierFromState(state, action.payload, identifier);
      else return removeFromState(state, identifier);
    }
  }
  return state;
}

export function getFailure(state: object, actionType: string, itemId?: string | number) {
  // @ts-ignore Right types not available
  if (state.failure) {
    let identifier = actionType;
    if (itemId) {
      identifier += "/" + itemId;
    }
    // @ts-ignore Right types not available
    return state.failure[identifier];
  }
}
