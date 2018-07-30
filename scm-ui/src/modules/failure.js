// @flow
import type { Action } from "../types/Action";

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

function removeFromState(state: Object, identifier: string) {
  const newState = {};
  for (let failureType in state) {
    if (failureType !== identifier) {
      newState[failureType] = state[failureType];
    }
  }
  return newState;
}

export default function reducer(
  state: Object = {},
  action: Action = { type: "UNKNOWN" }
): Object {
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
      return removeFromState(state, identifier);
    }
  }
  return state;
}

export function getFailure(
  state: Object,
  actionType: string,
  itemId?: string | number
) {
  if (state.failure) {
    let identifier = actionType;
    if (itemId) {
      identifier += "/" + itemId;
    }
    return state.failure[identifier];
  }
}
