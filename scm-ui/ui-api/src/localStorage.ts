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

import { useEffect, useMemo, useState } from "react";

type LocalStorageSetter<T> = (value: T | ((previousValue: T) => T)) => void;

const determineInitialValue = <T>(key: string, initialValue: T) => {
  try {
    const itemFromStorage = localStorage.getItem(key);
    return itemFromStorage ? JSON.parse(itemFromStorage) : initialValue;
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error(error);
    return initialValue;
  }
};

/**
 * Provides an api to access the browser's local storage for a given key.
 *
 * @param key The local storage key
 * @param initialValue Value to be used if the local storage does not yet have the given key defined
 */
export function useLocalStorage<T>(key: string, initialValue: T): [value: T, setValue: LocalStorageSetter<T>] {
  const initialValueOrValueFromStorage = useMemo(() => determineInitialValue(key, initialValue), [key, initialValue]);
  const [item, setItem] = useState<T>(initialValueOrValueFromStorage);

  useEffect(() => {
    const listener = (event: StorageEvent) => {
      if (event.key === key) {
        setItem(determineInitialValue(key, initialValue));
      }
    };
    window.addEventListener("storage", listener);
    return () => window.removeEventListener("storage", listener);
  }, [key, initialValue]);

  const setValue: LocalStorageSetter<T> = (newValue) => {
    // We've got to use setItem here to get the actual current value for item, not the one we got when this function was
    // created, in other words: We want to get rid of the dependency to item to get a similar behaviour as the setter
    // from useState.
    // (We also could wrap this function in a useCallback, but then we'd had to put this function in a dependency array
    // when we use this function so that we always refer to the current function. This is not the case for useState.)
    setItem((oldValue) => {
      const computedNewValue = newValue instanceof Function ? newValue(oldValue) : newValue;
      setItem(computedNewValue);
      const json = JSON.stringify(computedNewValue);
      localStorage.setItem(key, json);
      // storage event is not triggered in same tab
      window.dispatchEvent(
        new StorageEvent("storage", {
          key,
          oldValue: JSON.stringify(oldValue),
          newValue: json,
          storageArea: localStorage,
        })
      );
      return computedNewValue;
    });
  };

  return [item, setValue];
}
