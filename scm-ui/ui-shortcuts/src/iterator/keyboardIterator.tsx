import React, { FC, useCallback, useContext, useEffect, useMemo, useRef } from "react";
import { useTranslation } from "react-i18next";
import { useShortcut } from "../index";

type Callback = () => void;
type Callbacks = Array<Callback>;

type KeyboardIteratorContextType = {
  register: (callback: Callback) => number;
  deregister: (index: number) => void;
};

const KeyboardIteratorContext = React.createContext({} as KeyboardIteratorContextType);

export const KeyboardIteratorContextProvider: FC<{ initialIndex?: number }> = ({ children, initialIndex = -1 }) => {
  const [t] = useTranslation("plugins");
  const callbacks = useRef<Callbacks>([]);
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
        if (activeIndex.current === index || activeIndex.current >= callbacks.current.length) {
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

export function useKeyboardIteratorTarget(callback: Callback) {
  const { register, deregister } = useContext(KeyboardIteratorContext);
  useEffect(() => {
    const index = register(callback);
    return () => deregister(index);
  }, [callback, register, deregister]);
}
