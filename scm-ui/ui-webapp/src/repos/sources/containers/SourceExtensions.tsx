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

import React, { FC, useEffect, useState } from "react";
import { File, Repository } from "@scm-manager/ui-types";
import { useParams } from "react-router-dom";

import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useBranch, useChangesets, useSources } from "@scm-manager/ui-api";
import {
  REPOSITORY_CODE_SUB_ROUTES,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  useLayoutWide,
} from "@scm-manager/ui-core";

const extensionPointName = "repos.sources.extensions";

type Props = {
  // If true, the path is considered to be empty (used for root route).
  // This is needed as a workaround, because we use nested routes with splats (*), so that we cannot rely
  // on the path parameter taken from useParams to be empty for the root route with '*'.
  isRoot?: boolean;
};

type Params = {
  revision: string;
  extension: string;
  "*": string;
};

const useUrlParams = () => {
  const { revision, extension, "*": path } = useParams<Params>();
  return {
    revision: revision ? decodeURIComponent(revision) : undefined,
    path,
    extension,
  };
};

type PropsWithoutBranches = Props & {
  repository: Repository;
  baseUrl?: string;
  revision?: string;
  extension: string;
  path: string;
  sources?: File;
};

type PropsWithBranches = PropsWithoutBranches & {
  revision: string;
};

const useWaitForInitialLoad = (isFetching: boolean) => {
  const [renderedOnce, setRenderedOnce] = useState(false);

  useEffect(() => {
    if (!isFetching) {
      setRenderedOnce(true);
    }
  }, [isFetching]);

  return !renderedOnce && isFetching;
};

const SourceExtensionsWithBranches: FC<PropsWithBranches> = ({
  repository,
  baseUrl,
  revision,
  extension,
  sources,
  path,
}) => {
  const { isFetching, data: branch } = useBranch(repository, revision);
  const [t] = useTranslation("repos");

  const isLoading = useWaitForInitialLoad(isFetching);

  if (isLoading) {
    return <Loading />;
  }

  const resolvedRevision = branch?.revision;

  const extprops = {
    extension,
    repository,
    revision: revision ? encodeURIComponent(revision) : "",
    resolvedRevision,
    path,
    sources,
    baseUrl,
  };

  if (!binder.hasExtension(extensionPointName, extprops)) {
    return <Notification type="warning">{t("sources.extension.notBound")}</Notification>;
  }

  return <ExtensionPoint name={extensionPointName} props={extprops} />;
};

const SourceExtensionsWithoutBranches: FC<PropsWithoutBranches> = ({
  repository,
  baseUrl,
  revision,
  extension,
  sources,
  path,
}) => {
  const [t] = useTranslation("repos");

  const { isFetching, data: headChangeset } = useChangesets(repository, { limit: 1 });
  const isLoading = useWaitForInitialLoad(isFetching);

  if (isLoading) {
    return <Loading />;
  }

  const resolvedRevision = headChangeset?._embedded?.changesets[0]?.id;

  const extprops = {
    extension,
    repository,
    revision: revision ? encodeURIComponent(revision) : "",
    resolvedRevision,
    path,
    sources,
    baseUrl,
  };

  if (!binder.hasExtension(extensionPointName, extprops)) {
    return <Notification type="warning">{t("sources.extension.notBound")}</Notification>;
  }

  return <ExtensionPoint name={extensionPointName} props={extprops} />;
};

const SourceExtensions: FC<Props> = ({ isRoot }) => {
  const repository = useRepositoryFromParams();
  const baseUrl = useCurrentRepositoryBaseUrl();
  const { revision, path, extension } = useUrlParams();
  const { error, isLoading, data: sources } = useSources(repository, { revision, path });
  useLayoutWide();

  if (error) {
    return <ErrorNotification error={error} />;
  }
  if (isLoading) {
    return <Loading />;
  }
  if (!extension) {
    throw new Error("Extension is missing in url");
  }

  if (revision && repository._links.branches) {
    return (
      <SourceExtensionsWithBranches
        repository={repository}
        baseUrl={baseUrl + "/" + REPOSITORY_CODE_SUB_ROUTES.SOURCES}
        revision={revision}
        extension={extension}
        sources={sources}
        path={isRoot ? "" : path || ""}
      />
    );
  } else {
    return (
      <SourceExtensionsWithoutBranches
        repository={repository}
        baseUrl={baseUrl + "/" + REPOSITORY_CODE_SUB_ROUTES.SOURCES}
        revision={revision}
        extension={extension}
        sources={sources}
        path={isRoot ? "" : path || ""}
      />
    );
  }
};

export default SourceExtensions;
