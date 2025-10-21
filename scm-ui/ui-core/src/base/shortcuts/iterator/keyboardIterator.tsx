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

import React, { FC, useCallback, useContext, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useShortcut } from "../index";
import {
  Callback,
  CallbackIterator,
  CallbackRegistry,
  IterableCallback,
  useCallbackIterator,
} from "./callbackIterator";

const KeyboardIteratorContext = React.createContext<CallbackRegistry>({
  register: () => {
    if (process.env.NODE_ENV === "development") {
      // eslint-disable-next-line no-console
      console.warn("Keyboard iterator targets have to be declared inside a KeyboardIterator");
    }
    return 0;
  },
  deregister: () => {
    if (process.env.NODE_ENV === "development") {
      // eslint-disable-next-line no-console
      console.warn("Keyboard iterator targets have to be declared inside a KeyboardIterator");
    }
  },
  deregisterItem: () => {
    if (process.env.NODE_ENV === "development") {
      // eslint-disable-next-line no-console
      console.warn("Keyboard iterator targets have to be declared inside a KeyboardIterator");
    }
  },
  registerItem: () => {
    if (process.env.NODE_ENV === "development") {
      // eslint-disable-next-line no-console
      console.warn("Keyboard iterator targets have to be declared inside a KeyboardIterator");
    }
  },
});

export const useKeyboardIteratorItem = (item: Callback | CallbackIterator) => {
  const { register, deregister } = useContext(KeyboardIteratorContext);
  useEffect(() => {
    const index = register(item);
    return () => deregister(index);
  }, [item, register, deregister]);
};

export const useKeyboardIteratorItemV2 = (iterable: IterableCallback) => {
  const { registerItem, deregisterItem } = useContext(KeyboardIteratorContext);
  useEffect(() => {
    registerItem?.(iterable);
    return () => deregisterItem?.(iterable);
  }, [iterable, registerItem, deregisterItem]);
};

export const KeyboardSubIteratorContextProvider: FC<{ initialIndex?: number }> = ({ children, initialIndex }) => {
  const callbackIterator = useCallbackIterator(initialIndex);

  useKeyboardIteratorItem(callbackIterator);

  return <KeyboardIteratorContext.Provider value={callbackIterator}>{children}</KeyboardIteratorContext.Provider>;
};

export const KeyboardIteratorContextProvider: FC<{ initialIndex?: number }> = ({ children, initialIndex }) => {
  const [t] = useTranslation("commons");
  const callbackIterator = useCallbackIterator(initialIndex);

  useShortcut("k", callbackIterator.previous.bind(callbackIterator), {
    description: t("shortcuts.iterator.previous"),
  });

  useShortcut("j", callbackIterator.next.bind(callbackIterator), {
    description: t("shortcuts.iterator.next"),
  });

  useShortcut("tab", () => {
    callbackIterator.reset();

    return true;
  });

  return <KeyboardIteratorContext.Provider value={callbackIterator}>{children}</KeyboardIteratorContext.Provider>;
};

/**
 * @deprecated since version 3.8.0. Use {@link useKeyboardIteratorTargetV2} instead.
 * Use the {@link React.RefObject} returned from this hook to register a target to the nearest enclosing {@link KeyboardIterator} or {@link KeyboardSubIterator}.
 *
 * @example
 * const ref = useKeyboardIteratorTarget();
 * const target = <button ref={ref}>My Iteration Target</button>
 */
export function useKeyboardIteratorTarget(): React.RefCallback<HTMLElement> {
  const ref = useRef<HTMLElement>();
  const callback = useCallback(() => ref.current?.focus(), []);
  const refCallback: React.RefCallback<HTMLElement> = useCallback((el) => {
    if (el) {
      ref.current = el;
    }
  }, []);
  useKeyboardIteratorItem(callback);
  return refCallback;
}

/**
 * Use the {@link React.RefObject} returned from this hook to register a target to the nearest enclosing {@link KeyboardIterator} or {@link KeyboardSubIterator},
 * while respecting its expected index / position.
 *
 * @example
 * const ref = useKeyboardIteratorTarget({ expectedIndex: 0});
 * const target = <button ref={ref}>My Iteration Target</button>
 */
export function useKeyboardIteratorTargetV2({
  expectedIndex,
}: {
  expectedIndex: number;
}): React.RefCallback<HTMLElement> {
  const ref = useRef<HTMLElement>();
  const callback = useCallback(() => ref.current?.focus(), []);
  const cleanup = useCallback(() => ref.current?.blur(), []);
  const refCallback: React.RefCallback<HTMLElement> = useCallback((el) => {
    if (el) {
      ref.current = el;
    }
  }, []);
  useKeyboardIteratorItemV2({ item: callback, cleanup, expectedIndex });
  return refCallback;
}

/**
 * Allows keyboard users to iterate through a list of items, defined by enclosed {@link useKeyboardIteratorTarget} invocations.
 *
 * The order is determined by the render order of the target hooks.
 *
 * Press `k` to navigate backwards and `j` to navigate forward.
 * Pressing `tab` will reset the iterator to its initial state.
 *
 * Use the {@link KeyboardSubIterator} to wrap asynchronously loaded targets.
 */
export const KeyboardIterator: FC = ({ children }) => (
  <KeyboardIteratorContextProvider>{children}</KeyboardIteratorContextProvider>
);

/**
 * Allows deferred {@link useKeyboardIteratorTarget} invocations enclosed in this sub-iterator to be registered in the correct order within a {@link KeyboardIterator}.
 *
 * This is especially useful for extension points which might contain further iterable elements that are loaded asynchronously.
 *
 * @example
 *  <KeyboardIterator>
 *     <KeyboardSubIterator>
 *             <ExtensionPoint<extensionPoints.RepositoryOverviewTop>
 *               name="repository.overview.top"
 *               renderAll={true}
 *               props={{
 *                 page,
 *                 search,
 *                 namespace,
 *               }}
 *             />
 *     </KeyboardSubIterator>
 *     {groups.map((group) => {
 *        return <RepositoryGroupEntry group={group} key={group.name} />;
 *     })}
 *  </KeyboardIterator>
 */
export const KeyboardSubIterator: FC = ({ children }) => (
  <KeyboardSubIteratorContextProvider>{children}</KeyboardSubIteratorContextProvider>
);
