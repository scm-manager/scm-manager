// @flow
import type { Action } from "../types/Action";
import * as types from "./types";

const PENDING_SUFFIX = "_" + types.PENDING_SUFFIX;
const RESET_ACTIONTYPES = [
  types.SUCCESS_SUFFIX,
  types.FAILURE_SUFFIX,
  types.RESET_SUFFIX
];

function removeFromState(state: Object, identifier: string) {
  let newState = {};
  for (let childType in state) {
    if (childType !== identifier) {
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

export default function reducer(state: Object = {}, action: Action): Object {
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
        return removeFromState(state, identifier);
      }
    }
  }
  return state;
}

export function isPending(
  state: Object,
  actionType: string,
  itemId?: string | number
) {
  let type = actionType;
  if (itemId) {
    type += "/" + itemId;
  }
  if (state.pending && state.pending[type]) {
    return true;
  }
  return false;
}
