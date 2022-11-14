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

const INACTIVE_INDEX = -1;

export type Callback = () => void;

type Direction = "forward" | "backward";

/**
 * Restricts the api surface exposed by {@link CallbackIterator} so that we do not have to implement
 * the whole class when providing a default context.
 */
export type CallbackRegistry = {
  /**
   * Registers the given item and returns its index to use in {@link deregister}.
   */
  register: (item: Callback | CallbackIterator) => number;

  /**
   * Use the index returned from {@link register} to de-register.
   */
  deregister: (index: number) => void;
};

const isSubiterator = (item?: Callback | CallbackIterator): item is CallbackIterator =>
  item instanceof CallbackIterator;

const offset = (direction: Direction) => (direction === "forward" ? 1 : -1);

/**
 * ## Definition
 * - A list of callback functions and/or recursively nested iterators
 * - The iterator can move in either direction
 * - New items can be added/removed on-the-fly
 *
 * ## Terminology
 * - Item: Either a callback or a nested iterator
 * - Available: Item is a non-empty iterator OR a regular callback
 * - Inactive: Current index is -1
 * - Activate: Move iterator while in inactive state OR call regular callback
 *
 * ## Moving
 * When an iterator is moved in either direction, there are 4 cases:
 *
 * 1. The iterator is inactive => activate item at first available index from given direction
 * 1. The current item is a sub-iterator with more items in the given direction => move the sub-iterator
 * 1. The current item is a sub-iterator that has reached its bounds in the given direction => reset sub-iterator & activate item at next available index
 * 1. The current item is not a sub-iterator => activate item at next available index
 */
export class CallbackIterator implements CallbackRegistry {
  private parent?: CallbackIterator;

  constructor(
    private readonly activeIndexRef: MutableRefObject<number>,
    private readonly itemsRef: MutableRefObject<Array<Callback | CallbackIterator>>
  ) {}

  private get activeIndex() {
    return this.activeIndexRef.current;
  }

  private set activeIndex(newValue: number) {
    this.activeIndexRef.current = newValue;
  }

  private get items() {
    return this.itemsRef.current;
  }

  private get currentItem(): Callback | CallbackIterator | undefined {
    return this.items[this.activeIndex];
  }

  private get isInactive() {
    return this.activeIndex === INACTIVE_INDEX;
  }

  private get lastIndex() {
    return this.items.length - 1;
  }

  private firstIndex(direction: "forward" | "backward") {
    return direction === "forward" ? 0 : this.lastIndex;
  }

  private firstAvailableIndex(direction: Direction, fromIndex = this.firstIndex(direction)) {
    for (; direction === "forward" ? fromIndex < this.items.length : fromIndex >= 0; fromIndex += offset(direction)) {
      const callback = this.items[fromIndex];
      if (callback) {
        if (!isSubiterator(callback) || callback.hasNext(direction)) {
          return fromIndex;
        }
      }
    }
    return null;
  }

  private hasAvailableIndex(direction: Direction, fromIndex?: number) {
    return this.firstAvailableIndex(direction, fromIndex) !== null;
  }

  private activateCurrentItem(direction: Direction) {
    if (isSubiterator(this.currentItem)) {
      this.currentItem.move(direction);
    } else if (this.currentItem) {
      this.currentItem();
    }
  }

  private setIndexAndActivateCurrentItem(index: number | null, direction: Direction) {
    if (index !== null && index !== INACTIVE_INDEX) {
      this.activeIndex = index;
      this.activateCurrentItem(direction);
    }
  }

  private move(direction: Direction) {
    if (isSubiterator(this.currentItem) && this.currentItem.hasNext(direction)) {
      this.currentItem.move(direction);
    } else {
      if (isSubiterator(this.currentItem)) {
        this.currentItem.reset();
      }
      let nextIndex: number | null;
      if (this.isInactive) {
        nextIndex = this.firstAvailableIndex(direction);
      } else {
        nextIndex = this.firstAvailableIndex(direction, this.activeIndex + offset(direction));
      }
      this.setIndexAndActivateCurrentItem(nextIndex, direction);
    }
  }

  private hasNext(inDirection: Direction): boolean {
    if (this.isInactive) {
      return this.hasAvailableIndex(inDirection);
    }
    if (isSubiterator(this.currentItem) && this.currentItem.hasNext(inDirection)) {
      return true;
    }
    return this.hasAvailableIndex(inDirection, this.activeIndex + offset(inDirection));
  }

  public next() {
    if (this.hasNext("forward")) {
      return this.move("forward");
    }
  }

  public previous() {
    if (this.hasNext("backward")) {
      return this.move("backward");
    }
  }

  public reset() {
    this.activeIndex = INACTIVE_INDEX;
    for (const cb of this.items) {
      if (isSubiterator(cb)) {
        cb.reset();
      }
    }
  }

  public register(item: Callback | CallbackIterator) {
    if (isSubiterator(item)) {
      item.parent = this;
    }
    return this.items.push(item) - 1;
  }

  public deregister(index: number) {
    this.items.splice(index, 1);
    if (this.activeIndex === index || this.activeIndex >= this.items.length) {
      if (this.hasAvailableIndex("backward", index)) {
        this.setIndexAndActivateCurrentItem(this.firstAvailableIndex("backward", index), "backward");
      } else if (this.hasAvailableIndex("forward", index)) {
        this.setIndexAndActivateCurrentItem(this.firstAvailableIndex("forward", index), "backward");
      } else if (this.parent) {
        if (this.parent.hasNext("forward")) {
          this.parent.move("forward");
        } else if (this.parent.hasNext("backward")) {
          this.parent.move("backward");
        }
      } else {
        this.reset();
      }
    }
  }
}

export const useCallbackIterator = (initialIndex = INACTIVE_INDEX) => {
  const items = useRef<Array<Callback | CallbackIterator>>([]);
  const activeIndex = useRef<number>(initialIndex);
  return useMemo(() => new CallbackIterator(activeIndex, items), [activeIndex, items]);
};
