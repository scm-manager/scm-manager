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

const extensionPointName = "repos.sources.extensions";

type Props = {
  repository: Repository;
  baseUrl?: string;
};

type Params = {
  revision: string;
  path: string;
  extension: string;
};

const useUrlParams = () => {
  const { revision, path, extension } = useParams<Params>();
  return {
    revision: revision ? decodeURIComponent(revision) : undefined,
    path: path,
    extension
  };
};

type PropsWithoutBranches = Props & {
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
  path
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
    baseUrl
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
  path
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
    baseUrl
  };

  if (!binder.hasExtension(extensionPointName, extprops)) {
    return <Notification type="warning">{t("sources.extension.notBound")}</Notification>;
  }

  return <ExtensionPoint name={extensionPointName} props={extprops} />;
};

const SourceExtensions: FC<Props> = ({ repository, baseUrl }) => {
  const { revision, path, extension } = useUrlParams();
  const { error, isLoading, data: sources } = useSources(repository, { revision, path });

  if (error) {
    return <ErrorNotification error={error} />;
  }
  if (isLoading) {
    return <Loading />;
  }

  if (revision && repository._links.branches) {
    return (
      <SourceExtensionsWithBranches
        repository={repository}
        baseUrl={baseUrl}
        revision={revision}
        extension={extension}
        sources={sources}
        path={path}
      />
    );
  } else {
    return (
      <SourceExtensionsWithoutBranches
        repository={repository}
        baseUrl={baseUrl}
        revision={revision}
        extension={extension}
        sources={sources}
        path={path}
      />
    );
  }
};

export default SourceExtensions;
