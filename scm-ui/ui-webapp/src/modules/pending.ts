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
import * as types from "./types";

const PENDING_SUFFIX = "_" + types.PENDING_SUFFIX;
const RESET_ACTIONTYPES = [types.SUCCESS_SUFFIX, types.FAILURE_SUFFIX, types.RESET_SUFFIX];

function removeFromState(state: object, identifier: string) {
  const newState = {};
  for (const childType in state) {
    if (childType !== identifier) {
      // @ts-ignore Right types not available
      newState[childType] = state[childType];
    }
  }
  return newState;
}

function removeAllEntriesOfIdentifierFromState(state: object, payload: any, identifier: string) {
  const newState = {};
  for (const childType in state) {
    if (childType !== identifier && !childType.startsWith(identifier)) {
      // @ts-ignore Right types not available
      newState[childType] = state[childType];
    }
  }
  return newState;
}

function extractIdentifierFromPending(action: Action) {
  const type = action.type;
  let identifier = type.substring(0, type.length - PENDING_SUFFIX.length);
  if (action.itemId) {
    identifier += "/" + action.itemId;
  }
  return identifier;
}

export default function reducer(
  state: object = {},
  action: Action = {
    type: "UNKNOWN"
  }
): object {
  const type = action.type;
  if (type.endsWith(PENDING_SUFFIX)) {
    const identifier = extractIdentifierFromPending(action);
    return {
      ...state,
      [identifier]: true
    };
  } else {
    const index = type.lastIndexOf("_");
    if (index > 0) {
      const actionType = type.substring(index + 1);
      if (RESET_ACTIONTYPES.indexOf(actionType) >= 0 || action.resetPending) {
        let identifier = type.substring(0, index);
        if (action.itemId) {
          identifier += "/" + action.itemId;
        }
        if (action.payload) return removeAllEntriesOfIdentifierFromState(state, action.payload, identifier);
        else return removeFromState(state, identifier);
      }
    }
  }
  return state;
}

export function isPending(state: object, actionType: string, itemId?: string | number) {
  let type = actionType;
  if (itemId) {
    type += "/" + itemId;
  }
  // @ts-ignore Right types not available
  if (state.pending && state.pending[type]) {
    return true;
  }
  return false;
}
