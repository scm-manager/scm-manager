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
        .then((response) => response.json())
        .then((config: HgRepositoryConfiguration) => {
          setData(config);
        })
        .catch((e) => setError(e))
        .finally(() => setLoading(false));
    }
  }, [link]);

  return {
    isLoading,
    error,
    data,
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
      .catch((e) => setError(e))
      .finally(() => setLoading(false));
  };

  return {
    isLoading,
    error,
    update,
    updated,
  };
};
