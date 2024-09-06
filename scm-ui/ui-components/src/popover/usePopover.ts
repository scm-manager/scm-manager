/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import { Dispatch, useReducer, useRef } from "react";

type EnterTrigger = {
  type: "enter-trigger";
  offsetTop: number;
  offsetLeft: number;
};

type LeaveTrigger = {
  type: "leave-trigger";
};

type EnterPopover = {
  type: "enter-popover";
};

type LeavePopover = {
  type: "leave-popover";
};

export type Action = EnterTrigger | LeaveTrigger | EnterPopover | LeavePopover;

type State = {
  offsetTop?: number;
  offsetLeft?: number;
  onPopover: boolean;
  onTrigger: boolean;
};

const initialState = {
  onPopover: false,
  onTrigger: false,
};

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case "enter-trigger": {
      if (state.onPopover) {
        return state;
      }
      return {
        offsetTop: action.offsetTop,
        offsetLeft: action.offsetLeft,
        onTrigger: true,
        onPopover: false,
      };
    }
    case "leave-trigger": {
      if (state.onPopover) {
        return {
          ...state,
          onTrigger: false,
        };
      }
      return initialState;
    }
    case "enter-popover": {
      return {
        ...state,
        onPopover: true,
      };
    }
    case "leave-popover": {
      if (state.onTrigger) {
        return {
          ...state,
          onPopover: false,
        };
      }
      return initialState;
    }
  }
};

const dispatchDeferred = (dispatch: Dispatch<Action>, action: Action) => {
  setTimeout(() => dispatch(action), 250);
};

/**
 * @deprecated
 */
const usePopover = () => {
  const [state, dispatch] = useReducer(reducer, initialState);
  const triggerRef = useRef<HTMLElement | null>(null);

  const onMouseOver = () => {
    const current = triggerRef.current!;
    dispatchDeferred(dispatch, {
      type: "enter-trigger",
      offsetTop: current.offsetTop,
      offsetLeft: current.offsetLeft + current.offsetWidth / 2,
    });
  };

  const onMouseLeave = () => {
    dispatchDeferred(dispatch, {
      type: "leave-trigger",
    });
  };

  return {
    triggerProps: {
      onMouseOver,
      onMouseLeave,
      ref: (node: HTMLElement | null) => (triggerRef.current = node),
    },
    popoverProps: {
      dispatch,
      show: state.onPopover || state.onTrigger,
      offsetTop: state.offsetTop,
      offsetLeft: state.offsetLeft,
    },
  };
};

export default usePopover;
