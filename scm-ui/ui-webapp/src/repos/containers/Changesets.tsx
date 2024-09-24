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
import { Redirect, useRouteMatch } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useChangesets } from "@scm-manager/ui-api";
import { Branch, ChangesetCollection, Repository } from "@scm-manager/ui-types";
import {
  ChangesetList,
  ErrorNotification,
  LinkPaginator,
  Loading,
  Notification,
  urls,
} from "@scm-manager/ui-components";

export const usePage = () => {
  const match = useRouteMatch();
  return urls.getPageFromMatch(match);
};

type Props = {
  repository: Repository;
  branch?: Branch;
  url: string;
};

const Changesets: FC<Props> = ({ repository, branch, url }) => {
  const page = usePage();

  const { isLoading, error, data } = useChangesets(repository, { branch, page: page - 1 });

  return <ChangesetsPanel repository={repository} error={error} isLoading={isLoading} data={data} url={url} />;
};

type ChangesetsPanelProps = Props & {
  error: Error | null;
  isLoading: boolean;
  data?: ChangesetCollection;
};

export const ChangesetsPanel: FC<ChangesetsPanelProps> = ({ repository, error, isLoading, data, url }) => {
  const page = usePage();
  const [t] = useTranslation("repos");
  const changesets = data?._embedded?.changesets;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  if (data && data.pageTotal < page && page > 1) {
    return <Redirect to={`${urls.unescapeUrlForRoute(url)}/${data.pageTotal}`} />;
  }

  if (!data || !changesets || changesets.length === 0) {
    return <Notification type="info">{t("changesets.noChangesets")}</Notification>;
  }

  return (
    <div className="panel">
      <div className="panel-block">
        <ChangesetList repository={repository} changesets={changesets} />
      </div>
      <div className="panel-footer">
        <LinkPaginator collection={data} page={page} />
      </div>
    </div>
  );
};

export default Changesets;
