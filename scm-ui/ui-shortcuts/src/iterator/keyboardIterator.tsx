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

import React, { FC, useCallback, useContext, useEffect, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useShortcut } from "../index";
import { Callback, CallbackRegistry, Subiterator, useCallbackIterator } from "./callbackIterator";

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
});

export const useKeyboardIteratorItem = (item: Callback | Subiterator) => {
  const { register, deregister } = useContext(KeyboardIteratorContext);
  useEffect(() => {
    const index = register(item);
    return () => deregister(index);
  }, [item, register, deregister]);
};

export const KeyboardSubIteratorContextProvider: FC = ({ children }) => {
  const callbackIterator = useCallbackIterator();

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
