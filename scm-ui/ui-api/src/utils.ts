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

import { useCallback, useEffect, useRef } from "react";

export const createQueryString = (params: Record<string, string>) => {
  return Object.keys(params)
    .map((k) => encodeURIComponent(k) + "=" + encodeURIComponent(params[k]))
    .join("&");
};

export type CancelablePromise<T> = Promise<T> & { cancel: () => void };

export function makeCancelable<T>(promise: Promise<T>): CancelablePromise<T> {
  let isCanceled = false;
  const wrappedPromise = new Promise<T>((resolve, reject) => {
    promise
      .then((val) => (isCanceled ? reject({ isCanceled }) : resolve(val)))
      .catch((error) => (isCanceled ? reject({ isCanceled }) : reject(error)));
  });
  return Object.assign(wrappedPromise, {
    cancel() {
      isCanceled = true;
    },
  });
}

export function useCancellablePromise() {
  const promises = useRef<Array<CancelablePromise<unknown>>>();

  useEffect(() => {
    promises.current = promises.current || [];
    return function cancel() {
      promises.current?.forEach((p) => p.cancel());
      promises.current = [];
    };
  }, []);

  return useCallback(<T>(p: Promise<T>) => {
    const cPromise = makeCancelable(p);
    promises.current?.push(cPromise);
    return cPromise;
  }, []);
}
