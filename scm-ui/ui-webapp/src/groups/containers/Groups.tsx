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
import { Redirect, useLocation, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useGroups } from "@scm-manager/ui-api";
import { Group, GroupCollection } from "@scm-manager/ui-types";
import {
  CreateButton,
  LinkPaginator,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls,
} from "@scm-manager/ui-components";
import { GroupTable } from "./../components/table";

type GroupPageProps = {
  data?: GroupCollection;
  groups?: Group[];
  page: number;
  search?: string;
};

const GroupPage: FC<GroupPageProps> = ({ data, groups, page, search }) => {
  const [t] = useTranslation("groups");

  if (!data || !groups || groups.length === 0) {
    return <Notification type="info">{t("groups.noGroups")}</Notification>;
  }

  return (
    <>
      <GroupTable groups={groups} />
      <LinkPaginator collection={data} page={page} filter={search} />
    </>
  );
};

const Groups: FC = () => {
  const location = useLocation();
  const params = useParams();
  const search = urls.getQueryStringFromLocation(location);
  const page = urls.getPageFromMatch({ params });
  const { isLoading, error, data } = useGroups({ search, page: page - 1 });
  const [t] = useTranslation("groups");
  const groups = data?._embedded?.groups;
  const canCreateGroups = !!data?._links.create;
  if (data && data.pageTotal < page && page > 1) {
    return <Redirect to={`/groups/${data.pageTotal}`} />;
  }

  return (
    <Page
      title={t("groups.title")}
      subtitle={t("groups.subtitle")}
      loading={isLoading || !groups}
      error={error || undefined}
    >
      <GroupPage data={data} groups={groups} page={page} search={search} />
      {canCreateGroups ? <CreateButton link="/groups/create" label={t("groups.createButton")} /> : null}
      <PageActions>
        <OverviewPageActions
          showCreateButton={canCreateGroups}
          link="groups"
          label={t("groups.createButton")}
          searchPlaceholder={t("overview.filterGroup")}
        />
      </PageActions>
    </Page>
  );
};

export default Groups;
