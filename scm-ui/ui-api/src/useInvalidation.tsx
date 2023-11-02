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

import { useMutation } from "react-query";
import { apiClient } from "./apiclient";
import { useRequiredIndexLink } from "./base";

const useInvalidation = (link: string) => {
  const { mutate, isLoading, error, isSuccess } = useMutation<unknown, Error, string>((link) =>
    apiClient.post(link, {})
  );

  return {
    invalidate: () => mutate(link),
    isLoading,
    isSuccess,
    error,
  };
};

export const useInvalidateAllCaches = () => {
  const invalidateCacheLink = useRequiredIndexLink("invalidateCaches");
  return useInvalidation(invalidateCacheLink);
};

export const useInvalidateSearchIndices = () => {
  const invalidateSearchIndexLink = useRequiredIndexLink("invalidateSearchIndex");
  return useInvalidation(invalidateSearchIndexLink);
};
