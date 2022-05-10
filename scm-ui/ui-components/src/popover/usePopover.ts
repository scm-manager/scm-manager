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

const usePopover = () => {
  const [state, dispatch] = useReducer(reducer, initialState);
  const triggerRef = useRef<HTMLElement | null>(null);

  const onMouseOver = () => {
    const current = triggerRef.current;
    if (current) {
      dispatchDeferred(dispatch, {
        type: "enter-trigger",
        offsetTop: current.offsetTop,
        offsetLeft: current.offsetLeft + current.offsetWidth / 2,
      });
    }
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
