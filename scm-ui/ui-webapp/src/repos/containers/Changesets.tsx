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

import React, { FC } from "react";
import { useParams, Navigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useChangesets } from "@scm-manager/ui-api";
import { ChangesetCollection, Repository } from "@scm-manager/ui-types";
import { ChangesetList, LinkPaginator } from "@scm-manager/ui-components";
import {
  ErrorNotification,
  Notification,
  Loading,
  useDocumentTitle,
  useRepositoryFromParams,
  useCurrentRepositoryBaseUrl,
  REPOSITORY_CODE_SUB_ROUTES,
  REPOSITORY_SUB_ROUTES,
  useSourceNameFromParams,
} from "@scm-manager/ui-core";

export const usePage = () => {
  const { page } = useParams<{ page?: string }>();
  // Parse the string from the URL to a number, defaulting to 1.
  return parseInt(page || "1", 10);
};

const Changesets: FC = ({ ...props }) => {
  const page = usePage();
  const repository = useRepositoryFromParams();
  const sourceName = useSourceNameFromParams();

  const { isLoading, error, data } = useChangesets(repository, { revision: sourceName, page: page - 1 });
  const [t] = useTranslation("repos");
  const getDocumentTitle = () => {
    if (data?.pageTotal && data.pageTotal > 1 && page) {
      if (sourceName) {
        return t("changesets.commitsWithPageRevisionAndNamespaceName", {
          page,
          total: data.pageTotal,
          revision: sourceName,
          namespace: repository.namespace,
          name: repository.name,
        });
      } else {
        return t("changesets.commitsWithPageAndNamespaceName", {
          page,
          total: data.pageTotal,
          namespace: repository.namespace,
          name: repository.name,
        });
      }
    } else if (sourceName) {
      return t("changesets.commitsWithRevisionAndNamespaceName", {
        revision: sourceName,
        namespace: repository.namespace,
        name: repository.name,
      });
    } else {
      return t("changesets.commitsWithNamespaceName", {
        namespace: repository.namespace,
        name: repository.name,
      });
    }
  };
  useDocumentTitle(getDocumentTitle());

  return (
    <ChangesetsPanel
      isLoading={isLoading}
      error={error}
      data={data}
      repository={repository}
      sourceName={sourceName}
      {...props}
    />
  );
};

type ChangesetsPanelProps = {
  repository: Repository;
  sourceName?: string;
  error: Error | null;
  isLoading: boolean;
  data?: ChangesetCollection;
};

export const ChangesetsPanel: FC<ChangesetsPanelProps> = ({ repository, error, isLoading, data, sourceName }) => {
  const page = usePage();
  const [t] = useTranslation("repos");
  const changesets = data?._embedded?.changesets;
  const baseUrl = useCurrentRepositoryBaseUrl();

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  if (data && data.pageTotal < page && page > 1) {
    return <Navigate to={`../${data.pageTotal}`} />;
  }

  if (!data || !changesets || changesets.length === 0) {
    return <Notification type="info">{t("changesets.noChangesets")}</Notification>;
  }

  const baseUrl1 = sourceName
    ? `${baseUrl.substring(1)}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_SUB_ROUTES.BRANCH}/${sourceName}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}`
    : `${baseUrl.substring(1)}/${REPOSITORY_SUB_ROUTES.CODE}/${REPOSITORY_CODE_SUB_ROUTES.CHANGESETS}`;
  return (
    <div className="panel">
      <div className="panel-block">
        <ChangesetList repository={repository} changesets={changesets} sourceName={sourceName} />
      </div>
      <div className="panel-footer">
        <LinkPaginator collection={data} page={page} baseUrl={baseUrl1} />
      </div>
    </div>
  );
};

export default Changesets;
