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

import { MutableRefObject, useCallback, useMemo, useRef } from "react";

const INITIAL_INDEX = -1;
const SubiteratorSymbol = Symbol("Subiterator");

export type Callback = () => void;
type SubiteratorFunction = (forward: boolean, restart: boolean) => boolean;
export type Subiterator = { [SubiteratorSymbol]: SubiteratorFunction };

const isSubiterator = (input?: Callback | Subiterator): input is Subiterator =>
  typeof input === "object" && SubiteratorSymbol in input;

const executeCallback = (forward: boolean, currentCallback?: Callback | Subiterator) => {
  if (isSubiterator(currentCallback)) {
    return currentCallback[SubiteratorSymbol](forward, true);
  } else if (currentCallback) {
    currentCallback();
    return true;
  }
  return false;
};

export const navigate = (
  forward: boolean,
  activeIndexRef: MutableRefObject<number>,
  callbacks: Array<Callback | Subiterator>
) => {
  const activeIndex = activeIndexRef.current;
  const currentCallback = callbacks[activeIndex];
  if (!isSubiterator(currentCallback) || !currentCallback[SubiteratorSymbol](forward, false)) {
    if (activeIndex === INITIAL_INDEX) {
      let nextIndex = forward ? 0 : callbacks.length - 1;
      let hasNext = forward ? nextIndex < callbacks.length : nextIndex >= 0;
      while (hasNext && !executeCallback(forward, callbacks[nextIndex])) {
        nextIndex += forward ? 1 : -1;
        hasNext = forward ? nextIndex < callbacks.length : nextIndex >= 0;
      }
      if (hasNext) {
        activeIndexRef.current = nextIndex;
        return true;
      } else {
        return false;
      }
    } else if (forward ? activeIndex < callbacks.length - 1 : activeIndex > 0) {
      const nextIndex = activeIndex + (forward ? 1 : -1);
      if (executeCallback(forward, callbacks[nextIndex])) {
        activeIndexRef.current = nextIndex;
      }
    } else {
      return false;
    }
  }
  return true;
};

const useIteratorRefs = (initialIndex: number) => {
  const callbacks = useRef<Array<Callback | Subiterator>>([]);
  const activeIndex = useRef<number>(initialIndex);
  return useMemo(() => ({ activeIndex, callbacks } as const), []);
};

export type CallbackIterator = {
  register: (item: Callback | Subiterator) => number;
  deregister: (index: number) => void;
};

const createCallbackIterator = (
  activeIndex: MutableRefObject<number>,
  callbacks: MutableRefObject<Array<Callback | Subiterator>>
): CallbackIterator => ({
  register: (callback: Callback | Subiterator) => callbacks.current.push(callback) - 1,
  deregister: (index: number) => {
    callbacks.current.splice(index, 1);
    if (callbacks.current.length === 0) {
      activeIndex.current = INITIAL_INDEX;
    } else if (activeIndex.current === index || activeIndex.current >= callbacks.current.length) {
      if (activeIndex.current > 0) {
        activeIndex.current -= 1;
      }
      executeCallback(false, callbacks.current[activeIndex.current]);
    }
  },
});

export const createSubiterator = (
  activeIndex: MutableRefObject<number>,
  callbacks: MutableRefObject<Array<Callback | Subiterator>>
): Subiterator => ({
  [SubiteratorSymbol]: (forward, restart) => {
    if (restart) {
      activeIndex.current = INITIAL_INDEX;
    }
    return navigate(forward, activeIndex, callbacks.current);
  },
});

export const useCallbackIterator = (initialIndex: number) => {
  const { activeIndex, callbacks } = useIteratorRefs(initialIndex);
  const value = useMemo(() => createCallbackIterator(activeIndex, callbacks), [activeIndex, callbacks]);
  const next = useCallback(() => navigate(true, activeIndex, callbacks.current), [activeIndex, callbacks]);
  const previous = useCallback(() => navigate(false, activeIndex, callbacks.current), [activeIndex, callbacks]);
  const reset = useCallback(() => (activeIndex.current = INITIAL_INDEX), [activeIndex]);

  return useMemo(
    () => ({ activeIndex, callbacks, value, next, previous, reset } as const),
    [activeIndex, callbacks, next, previous, reset, value]
  );
};
