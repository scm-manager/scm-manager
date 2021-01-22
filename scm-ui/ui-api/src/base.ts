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

import { IndexResources, Link } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient } from "@scm-manager/ui-components";

export type ApiResult<T> = {
  isLoading: boolean;
  error: Error | null;
  data?: T;
};

export const useIndex = (): ApiResult<IndexResources> => {
  return useQuery<IndexResources, Error>("index", () => apiClient.get("/").then(response => response.json()));
};

export const useIndexLink = (name: string): string | undefined => {
  const { data } = useIndex();
  if (!data) {
    throw new Error("could not find index data");
  }
  const linkObject = data._links[name] as Link;
  if (linkObject) {
    return linkObject.href;
  }
};

export const useIndexLinks = () => {
  const { data } = useIndex();
  if (!data) {
    throw new Error("could not find index data");
  }
  return data._links;
};

export const useRequiredIndexLink = (name: string): string => {
  const link = useIndexLink(name);
  if (!link) {
    throw new Error(`Could not find link ${name} in index resource`);
  }
  return link;
};

export const useIndexJsonResource = <T>(name: string): ApiResult<T> => {
  const link = useIndexLink(name);
  return useQuery<T, Error>(name, () => apiClient.get(link!).then(response => response.json()), {
    // TODO does this make sense
    enabled: !!link
  });
};
