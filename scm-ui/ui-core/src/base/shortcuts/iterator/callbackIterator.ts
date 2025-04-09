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

import { MutableRefObject, useMemo, useRef } from "react";

const INACTIVE_INDEX = -1;

export type Callback = () => void;

export type IterableCallback = {
  item: Callback | CallbackIterator;
  expectedIndex: number;
  cleanup?: Callback;
};

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
  /**
   * Registers the given iterable item while maintaining the order of the expected index of each item.
   */
  registerItem?: (iterable: IterableCallback) => void;
  /**
   * Removes the passed iterable item.
   */
  deregisterItem?: (iterable: IterableCallback) => void;
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
 * - Cleanup: A callback that is called, if the corresponding item is removed
 * - Iterable: A wrapper containing an item, a cleanup callback and the index that this iterable is expected to be at
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
    private readonly itemsRef: MutableRefObject<Array<IterableCallback>>
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

  private get currentItem(): IterableCallback | undefined {
    return this.items[this.activeIndex];
  }

  private get isInactive() {
    return this.activeIndex === INACTIVE_INDEX;
  }

  private get lastIndex() {
    return this.items.length - 1;
  }

  private firstIndex = (direction: "forward" | "backward") => {
    return direction === "forward" ? 0 : this.lastIndex;
  };

  private firstAvailableIndex = (direction: Direction, fromIndex = this.firstIndex(direction)) => {
    for (; direction === "forward" ? fromIndex < this.items.length : fromIndex >= 0; fromIndex += offset(direction)) {
      const iterableCallback = this.items[fromIndex];
      if (iterableCallback) {
        if (!isSubiterator(iterableCallback.item) || iterableCallback.item.hasNext(direction)) {
          return fromIndex;
        }
      }
    }
    return null;
  };

  private hasAvailableIndex = (direction: Direction, fromIndex?: number) => {
    return this.firstAvailableIndex(direction, fromIndex) !== null;
  };

  private activateCurrentItem = (direction: Direction) => {
    if (isSubiterator(this.currentItem?.item)) {
      this.currentItem?.item.move(direction);
    } else if (this.currentItem) {
      this.currentItem.item();
    }
  };

  private setIndexAndActivateCurrentItem = (index: number | null, direction: Direction) => {
    this.currentItem?.cleanup?.();
    if (index !== null && index !== INACTIVE_INDEX) {
      this.activeIndex = index;
      this.activateCurrentItem(direction);
    }
  };

  private move = (direction: Direction) => {
    if (isSubiterator(this.currentItem?.item) && this.currentItem?.item.hasNext(direction)) {
      this.currentItem?.item.move(direction);
    } else {
      if (isSubiterator(this.currentItem?.item)) {
        this.currentItem?.item.reset();
      }
      let nextIndex: number | null;
      if (this.isInactive) {
        nextIndex = this.firstAvailableIndex(direction);
      } else {
        nextIndex = this.firstAvailableIndex(direction, this.activeIndex + offset(direction));
      }
      this.setIndexAndActivateCurrentItem(nextIndex, direction);
    }
  };

  private hasNext = (inDirection: Direction): boolean => {
    if (this.isInactive) {
      return this.hasAvailableIndex(inDirection);
    }
    if (isSubiterator(this.currentItem?.item) && this.currentItem?.item.hasNext(inDirection)) {
      return true;
    }
    return this.hasAvailableIndex(inDirection, this.activeIndex + offset(inDirection));
  };

  public next = () => {
    if (this.hasNext("forward")) {
      return this.move("forward");
    }
  };

  public previous = () => {
    if (this.hasNext("backward")) {
      return this.move("backward");
    }
  };

  public reset = () => {
    this.activeIndex = INACTIVE_INDEX;
    for (const cb of this.items) {
      if (isSubiterator(cb.item)) {
        cb.item.reset();
      }
    }
  };

  public register = (item: Callback | CallbackIterator) => {
    const expectedIndex = this.items.length;
    this.registerItem({ item, expectedIndex });
    return expectedIndex;
  };

  public deregister = (index: number) => {
    if (this.items[index]) {
      this.deregisterItem(this.items[index]);
    }
  };

  public deregisterItem = (iterable: IterableCallback) => {
    const itemIndex = this.items.findIndex((value) => value.expectedIndex === iterable.expectedIndex);
    if (itemIndex === -1) {
      return;
    }

    const removedIterable = this.items[itemIndex];
    removedIterable.cleanup?.();

    this.items.splice(itemIndex, 1);
    if (this.activeIndex >= itemIndex) {
      if (this.hasAvailableIndex("backward")) {
        this.setIndexAndActivateCurrentItem(this.firstAvailableIndex("backward", itemIndex), "backward");
      } else if (this.hasAvailableIndex("forward")) {
        this.setIndexAndActivateCurrentItem(this.firstAvailableIndex("forward", itemIndex), "forward");
      } else if (this.parent?.hasNext("forward")) {
        this.parent?.move("forward");
      } else if (this.parent?.hasNext("backward")) {
        this.parent?.move("backward");
      } else {
        this.reset();
      }
    }
  };

  public registerItem = (iterable: IterableCallback) => {
    if (isSubiterator(iterable.item)) {
      iterable.item.parent = this;
    }

    const insertAt = this.items.findIndex((value) => value.expectedIndex > iterable.expectedIndex);

    if (insertAt === -1) {
      this.items.push(iterable);
    } else {
      this.items.splice(insertAt, 0, iterable);
    }
  };
}

export const useCallbackIterator = (initialIndex = INACTIVE_INDEX) => {
  const items = useRef<Array<IterableCallback>>([]);
  const activeIndex = useRef<number>(initialIndex);
  return useMemo(() => new CallbackIterator(activeIndex, items), [activeIndex, items]);
};
