// @flow
import type { Action } from "../types/Action";

const PENDING_SUFFIX = "_PENDING";
const RESET_PATTERN = /^(.*)_(SUCCESS|FAILURE|RESET)$/;

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
    const matches = RESET_PATTERN.exec(type);
    if (matches) {
      return removeFromState(state, matches[1]);
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
