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

  if (data && data.pageTotal < page) {
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
