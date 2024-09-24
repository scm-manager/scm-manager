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

import { ApiResult, useIndexJsonResource, useRequiredIndexLink } from "./base";
import { Namespace, NamespaceCollection, NamespaceStrategies } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient } from "./apiclient";
import { concat } from "./urls";

export const useNamespaces = () => {
  return useIndexJsonResource<NamespaceCollection>("namespaces");
};

export const useNamespace = (name: string): ApiResult<Namespace> => {
  const namespacesLink = useRequiredIndexLink("namespaces");
  return useQuery<Namespace, Error>(["namespace", name], () =>
    apiClient.get(concat(namespacesLink, name)).then((response) => response.json())
  );
};

export const useNamespaceStrategies = () => {
  return useIndexJsonResource<NamespaceStrategies>("namespaceStrategies");
};
