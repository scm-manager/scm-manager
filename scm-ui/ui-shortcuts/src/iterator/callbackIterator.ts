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

import { MutableRefObject, useMemo, useRef } from "react";

const INITIAL_INDEX = -1;

const SubiteratorSymbol = Symbol("Subiterator");

export type Callback = () => void;

type SubiteratorFunction = (direction: Direction, withReset: boolean) => void;

type Direction = "next" | "previous";

export type Subiterator = {
  [SubiteratorSymbol]: SubiteratorFunction;
  has(direction: Direction): boolean;
  reset: () => void;
  setParent(parent: CallbackIterator): void;
};

const isSubiterator = (input?: Callback | Subiterator): input is Subiterator =>
  typeof input === "object" && SubiteratorSymbol in input;

class CallbackIterator implements CallbackRegistry, Subiterator {
  private parent?: CallbackIterator;

  constructor(
    private readonly activeIndexRef: MutableRefObject<number>,
    private readonly callbacksRef: MutableRefObject<Array<Callback | Subiterator>>
  ) {}

  [SubiteratorSymbol]: SubiteratorFunction = (direction, withReset) => {
    if (withReset) {
      this.reset();
    }

    this.navigate(direction);
  };

  private get hasCallbacks() {
    return this.callbacks.length > 0;
  }

  private get currentCallback() {
    return this.callbacks[this.activeIndex];
  }

  private get activeIndex() {
    return this.activeIndexRef.current;
  }

  private set activeIndex(newValue: number) {
    this.activeIndexRef.current = newValue;
  }

  private isInBounds(direction: Direction, index: number) {
    return direction === "next" ? index < this.callbacks.length : index >= 0;
  }

  private executeCallback(direction: Direction, currentCallback: Callback | Subiterator) {
    if (isSubiterator(currentCallback)) {
      return currentCallback[SubiteratorSymbol](direction, true);
    } else if (currentCallback) {
      currentCallback();
    }
  }

  private get callbacks() {
    return this.callbacksRef.current;
  }

  private get isInactive() {
    return this.activeIndex === INITIAL_INDEX;
  }

  private nextAvailableIndex(direction: Direction, start = direction === "next" ? 0 : this.callbacks.length - 1) {
    for (; this.isInBounds(direction, start); start += direction === "next" ? 1 : -1) {
      const callback = this.callbacks[start];
      if (!isSubiterator(callback) || callback.has(direction)) {
        return start;
      }
    }
    return -1;
  }

  private navigate = (direction: Direction) => {
    if (isSubiterator(this.currentCallback) && this.currentCallback.has(direction)) {
      this.currentCallback[SubiteratorSymbol](direction, false);
    } else {
      if (isSubiterator(this.currentCallback)) {
        this.currentCallback.reset();
      }
      if (this.isInactive) {
        this.activeIndex = this.nextAvailableIndex(direction);
      } else {
        this.activeIndex = this.nextAvailableIndex(direction, this.activeIndex + (direction === "next" ? 1 : -1));
      }
      this.executeCallback(direction, this.currentCallback);
    }
  };

  public setParent(parent: CallbackIterator): void {
    this.parent = parent;
  }

  public has(direction: Direction): boolean {
    if (isSubiterator(this.currentCallback) && this.currentCallback.has(direction)) {
      return true;
    } else if (this.isInactive) {
      return this.nextAvailableIndex(direction) !== -1;
    }
    return this.nextAvailableIndex(direction, this.activeIndex + (direction === "next" ? 1 : -1)) !== -1;
  }

  public deregister = (index: number) => {
    this.callbacks.splice(index, 1);
    if (!this.isInactive && !this.hasCallbacks) {
      this.reset();
      if (this.parent) {
        if (this.parent.has("next")) {
          this.parent.navigate("next");
        } else if (this.parent.has("previous")) {
          this.parent.navigate("previous");
        }
      }
    } else if (this.activeIndex >= this.callbacks.length) {
      this.activeIndex = this.callbacks.length - 1;
      this.executeCallback("previous", this.callbacks[this.activeIndex]);
    } else if (this.activeIndex === index) {
      let nextIndex: number;
      if ((nextIndex = this.nextAvailableIndex("previous", index)) !== -1) {
        this.activeIndex = nextIndex;
        this.executeCallback("previous", this.callbacks[this.activeIndex]);
      } else if ((nextIndex = this.nextAvailableIndex("next", index)) !== -1) {
        this.activeIndex = nextIndex;
        this.executeCallback("next", this.callbacks[this.activeIndex]);
      }
    }
  };

  public register = (callback: Callback | Subiterator) => {
    if (isSubiterator(callback)) {
      callback.setParent(this);
    }
    return this.callbacks.push(callback) - 1;
  };

  public reset() {
    this.activeIndex = INITIAL_INDEX;
    for (const cb of this.callbacks) {
      if (isSubiterator(cb)) {
        cb.reset();
      }
    }
  }

  public next() {
    if (this.has("next")) {
      return this.navigate("next");
    }
  }

  public previous() {
    if (this.has("previous")) {
      return this.navigate("previous");
    }
  }
}

const useIteratorRefs = (initialIndex: number) => {
  const callbacks = useRef<Array<Callback | Subiterator>>([]);
  const activeIndex = useRef<number>(initialIndex);
  return useMemo(() => ({ activeIndex, callbacks } as const), []);
};

export type CallbackRegistry = {
  register: (item: Callback | Subiterator) => number;
  deregister: (index: number) => void;
};

export const useCallbackIterator = (initialIndex = INITIAL_INDEX) => {
  const { activeIndex, callbacks } = useIteratorRefs(initialIndex);
  return useMemo(() => new CallbackIterator(activeIndex, callbacks), [activeIndex, callbacks]);
};
