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

import { useParams } from "react-router-dom";
import { useNamespace } from "@scm-manager/ui-api";
import { Namespace } from "@scm-manager/ui-types";

export const SINGLE_NAMESPACE_BASE_ROUTE = "namespace";
export const NAMESPACE_SUB_ROUTES = {
  INFO: "info",
  SETTINGS: "settings",
};
export const NAMESPACE_SETTINGS_SUB_ROUTES = {
  PERMISSIONS: "permissions",
};

export const useNamespaceFromParams = () => {
  const { namespace: namespaceName } = useParams<{ namespace: string }>();
  if (!namespaceName) {
    throw new Error("No namespace in url");
  }
  const { data: namespace } = useNamespace(namespaceName);
  if (!namespace) {
    throw new Error(`Namespace ${namespace} not found`);
  }
  return namespace;
};

export const useCurrentNamespaceBaseUrl = () => {
  const namespace = useNamespaceFromParams();
  return getNamespaceBaseUrl(namespace.namespace);
};

export function getNamespaceBaseUrl(namespace: string | Namespace): string {
  if (typeof namespace === "string") {
    return `/${SINGLE_NAMESPACE_BASE_ROUTE}/${namespace}`;
  } else {
    return `/${SINGLE_NAMESPACE_BASE_ROUTE}/${namespace.namespace}`;
  }
}
