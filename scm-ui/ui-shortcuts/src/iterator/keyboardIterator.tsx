import React, { FC, useCallback, useContext, useEffect, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useShortcut } from "../index";

type Callback = () => void;

type KeyboardIteratorContextType = {
  register: (callback: Callback) => number;
  deregister: (index: number) => void;
};

const KeyboardIteratorContext = React.createContext<KeyboardIteratorContextType>({
  register: () => {
    throw new Error("Keyboard iterator targets have to be declared inside a KeyboardIterator");
  },
  deregister: () => {
    throw new Error("Keyboard iterator targets have to be declared inside a KeyboardIterator");
  },
});

export const KeyboardIteratorContextProvider: FC<{ initialIndex?: number }> = ({ children, initialIndex = -1 }) => {
  const [t] = useTranslation("commons");
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
      activeIndex.current = callbacks.current.length - 1;
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
    description: t("shortcuts.iterator.previous"),
  });

  useShortcut("k", navigateForward, {
    description: t("shortcuts.iterator.next"),
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
export function useKeyboardIteratorTarget(): React.RefCallback<HTMLElement> {
  const ref = useRef<HTMLElement>();
  const callback = useCallback(() => ref.current?.focus(), []);
  const refCallback: React.RefCallback<HTMLElement> = useCallback((el) => {
    if (el) {
      ref.current = el;
    }
  }, []);
  useKeyboardIteratorCallback(callback);
  return refCallback;
}

/**
 * Allows keyboard users to iterate through a list of items, defined by enclosed {@link useKeyboardIteratorTarget} invocations.
 *
 * The order is determined by the render order of the target hooks.
 *
 * Press `j` to navigate backwards and `k` to navigate forward.
 * Pressing `tab` will reset the iterator to its initial state.
 */
export const KeyboardIterator: FC = ({ children }) => (
  <KeyboardIteratorContextProvider>{children}</KeyboardIteratorContextProvider>
);
