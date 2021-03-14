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
 *
 */

import { useEffect, useState } from "react";
import { apiClient } from "@scm-manager/ui-components";
import { HalRepresentation, Link, Repository } from "@scm-manager/ui-types";

export type HgRepositoryConfiguration = HalRepresentation & {
  encoding?: string;
};

export const useHgRepositoryConfiguration = (repository: Repository) => {
  const [isLoading, setLoading] = useState(false);
  const [data, setData] = useState<HgRepositoryConfiguration | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const link = (repository._links.configuration as Link)?.href;
  useEffect(() => {
    setError(null);
    if (link) {
      setLoading(true);
      apiClient
        .get(link)
        .then(response => response.json())
        .then((config: HgRepositoryConfiguration) => {
          setData(config);
        })
        .catch(e => setError(e))
        .finally(() => setLoading(false));
    }
  }, [link]);

  return {
    isLoading,
    error,
    data
  };
};

export const useUpdateHgRepositoryConfiguration = () => {
  const [isLoading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [updated, setUpdated] = useState(false);

  const update = (configuration: HgRepositoryConfiguration) => {
    if (!configuration._links.update) {
      throw new Error("no update link on configuration");
    }
    const link = (configuration._links.update as Link).href;
    setLoading(true);
    setUpdated(false);
    setError(null);
    apiClient
      .put(link, configuration, "application/vnd.scmm-hgConfig-repo+json;v=2")
      .then(() => setUpdated(true))
      .catch(e => setError(e))
      .finally(() => setLoading(false));
  };

  return {
    isLoading,
    error,
    update,
    updated
  };
};
