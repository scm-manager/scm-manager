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

import React, { createContext, FC, useCallback, useContext, useEffect, useMemo, useState } from "react";

type LocalStorage = {
  getItem: <T>(key: string, fallback: T) => T;
  setItem: <T>(key: string, value: T) => void;
  preload: <T>(key: string, initialValue: T) => void;
};

const LocalStorageContext = createContext<LocalStorage>(null as unknown as LocalStorage);

/**
 * Cache provider for local storage which enables listening to changes and triggering re-renders when writing.
 *
 * Only required once as a wrapper for the whole application.
 *
 * @see useLocalStorage
 */
export const LocalStorageProvider: FC = ({ children }) => {
  const [localStorageCache, setLocalStorageCache] = useState<Record<string, unknown>>({});

  const setItem = useCallback(<T,>(key: string, value: T) => {
    localStorage.setItem(key, JSON.stringify(value));
    setLocalStorageCache((prevState) => ({
      ...prevState,
      [key]: value,
    }));
  }, []);

  const getItem = useCallback(
    <T,>(key: string, fallback: T): T => (key in localStorageCache ? (localStorageCache[key] as T) : fallback),
    [localStorageCache]
  );

  const preload = useCallback(
    <T,>(key: string, initialValue: T) => {
      if (!(key in localStorageCache)) {
        let initialLoadResult: T | undefined;
        try {
          const item = localStorage.getItem(key);
          initialLoadResult = item ? JSON.parse(item) : initialValue;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
          initialLoadResult = initialValue;
        }
        setItem(key, initialLoadResult);
      }
    },
    [localStorageCache, setItem]
  );

  return (
    <LocalStorageContext.Provider value={useMemo(() => ({ getItem, setItem, preload }), [getItem, preload, setItem])}>
      {children}
    </LocalStorageContext.Provider>
  );
};

/**
 * Provides an api to access the browser's local storage for a given key.
 *
 * @param key The local storage key
 * @param initialValue Value to be used if the local storage does not yet have the given key defined
 */
export function useLocalStorage<T>(
  key: string,
  initialValue: T
): [value: T, setValue: (value: T | ((previousConfig: T) => T)) => void] {
  const { getItem, setItem, preload } = useContext(LocalStorageContext);
  const value = useMemo(() => getItem(key, initialValue), [getItem, initialValue, key]);
  const setValue = useCallback(
    (newValue: T | ((previousConfig: T) => T)) =>
      // @ts-ignore T could be a function type, although this does not make sense because function types are not serializable to json
      setItem(key, typeof newValue === "function" ? newValue(value) : newValue),
    // eslint does not understand generics in certain circumstances
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [key, setItem, value]
  );
  useEffect(() => preload(key, initialValue), [initialValue, key, preload]);
  return useMemo(() => [value, setValue], [setValue, value]);
}
