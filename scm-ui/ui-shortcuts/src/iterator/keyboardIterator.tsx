import React, { FC, useCallback, useContext, useEffect, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useShortcut } from "../index";

type Callback = () => void;

type KeyboardIteratorContextType = {
  register: (callback: Callback) => number;
  deregister: (index: number) => void;
};

const KeyboardIteratorContext = React.createContext({} as KeyboardIteratorContextType);

export const KeyboardIteratorContextProvider: FC<{ initialIndex?: number }> = ({ children, initialIndex = -1 }) => {
  const [t] = useTranslation("plugins");
  const callbacks = useRef<Array<Callback>>([]);
  const activeIndex = useRef<number>(initialIndex);
  const executeCallback = useCallback((index: number) => callbacks.current[index](), []);
  const navigateBackward = useCallback(() => {
    if (activeIndex.current === -1) {
      activeIndex.current = 0;
      executeCallback(activeIndex.current);
    } else if (activeIndex.current > 0) {
      activeIndex.current -= 1;
      executeCallback(activeIndex.current);
    }
  }, [executeCallback]);
  const navigateForward = useCallback(() => {
    if (activeIndex.current === -1) {
      activeIndex.current = 0;
      executeCallback(activeIndex.current);
    } else if (activeIndex.current < callbacks.current.length - 1) {
      activeIndex.current += 1;
      executeCallback(activeIndex.current);
    }
  }, [executeCallback]);
  const value = useMemo(
    () => ({
      register: (callback: () => void) => callbacks.current.push(callback) - 1,
      deregister: (index: number) => {
        callbacks.current.splice(index, 1);
        if (callbacks.current.length === 0) {
          activeIndex.current = -1;
        } else if (activeIndex.current === index || activeIndex.current >= callbacks.current.length) {
          if (activeIndex.current > 0) {
            activeIndex.current -= 1;
          }
          executeCallback(activeIndex.current);
        }
      },
    }),
    [executeCallback]
  );
  useShortcut("j", navigateBackward, {
    description: t("scm-review-plugin.shortcuts.previousPullRequest"),
  });
  useShortcut("k", navigateForward, {
    description: t("scm-review-plugin.shortcuts.nextPullRequest"),
  });
  useShortcut("tab", () => {
    activeIndex.current = -1;

    return true;
  });
  return <KeyboardIteratorContext.Provider value={value}>{children}</KeyboardIteratorContext.Provider>;
};

export const useKeyboardIteratorCallback = (callback: Callback) => {
  const { register, deregister } = useContext(KeyboardIteratorContext);
  useEffect(() => {
    const index = register(callback);
    return () => deregister(index);
  }, [callback, register, deregister]);
};

/**
 * Use the {@link React.RefObject} returned from this hook to register a target to the nearest enclosing {@link KeyboardIterator}.
 *
 * @example
 * const ref = useKeyboardIteratorTarget();
 * const target = <button ref={ref}>My Iteration Target</button>
 */
export function useKeyboardIteratorTarget() {
  const ref = useRef<HTMLElement>(null);
  const callback = useCallback(() => ref.current?.focus(), []);
  useKeyboardIteratorCallback(callback);
  return ref;
}

/**
 * Allows users to iterate through a pre-defined list of items, provided by enclosed {@link useKeyboardIteratorTarget} invocations.
 *
 * The order is determined by the render order of the target hooks.
 *
 * Users can press `j` to navigate backwards and `k` to navigate forward.
 * Once a user presses `tab`, the iterator is reset to its initial state.
 *
 * Note: When at the end or beginning of the list, moving forward or backward respectively will not continuously trigger the last/first callback.
 */
export const KeyboardIterator: FC = ({ children }) => (
  <KeyboardIteratorContextProvider>{children}</KeyboardIteratorContextProvider>
);
