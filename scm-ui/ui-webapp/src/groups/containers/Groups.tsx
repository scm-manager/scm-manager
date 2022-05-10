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
import { useTranslation } from "react-i18next";
import { useLocation, useParams } from "react-router-dom";
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
import { useGroups } from "@scm-manager/ui-api";

const Groups: FC = () => {
  const location = useLocation();
  const params = useParams();
  const search = urls.getQueryStringFromLocation(location);
  const page = urls.getPageFromMatch({ params });
  const { isLoading, error, data: list } = useGroups({ search, page: page - 1 });
  const [t] = useTranslation("groups");
  const groups = list?._embedded.groups;
  const canCreateGroups = !!list?._links.create;

  const renderGroupTable = () => {
    if (list && groups && groups.length > 0) {
      return (
        <>
          <GroupTable groups={groups} />
          <LinkPaginator collection={list} page={page} filter={urls.getQueryStringFromLocation(location)} />
        </>
      );
    }
    return <Notification type="info">{t("groups.noGroups")}</Notification>;
  };

  return (
    <Page
      title={t("groups.title")}
      subtitle={t("groups.subtitle")}
      loading={isLoading || !groups}
      error={error || undefined}
    >
      {renderGroupTable()}
      {canCreateGroups ? <CreateButton label={t("create-group-button.label")} link="/groups/create" /> : null}
      <PageActions>
        <OverviewPageActions
          showCreateButton={canCreateGroups}
          link="groups"
          label={t("create-group-button.label")}
          searchPlaceholder={t("overview.filterGroup")}
        />
      </PageActions>
    </Page>
  );
};

export default Groups;
