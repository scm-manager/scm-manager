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
import { useUsers } from "@scm-manager/ui-api";
import { User, UserCollection } from "@scm-manager/ui-types";
import { CreateButton, LinkPaginator, OverviewPageActions, Page, PageActions, urls } from "@scm-manager/ui-components";
import { Notification } from "@scm-manager/ui-core";
import { UserTable } from "./../components/table";

type UserPageProps = {
  data?: UserCollection;
  users?: User[];
  page: number;
  search?: string;
};

const UserPage: FC<UserPageProps> = ({ data, users, page, search }) => {
  const [t] = useTranslation("users");

  if (!data || !users || users.length === 0) {
    return <Notification type="info">{t("users.noUsers")}</Notification>;
  }

  return (
    <>
      <UserTable users={users} />
      <LinkPaginator collection={data} page={page} filter={search} />
    </>
  );
};

const Users: FC = () => {
  const location = useLocation();
  const params = useParams();
  const search = urls.getQueryStringFromLocation(location);
  const page = urls.getPageFromMatch({ params });
  const { isLoading, error, data } = useUsers({ page: page - 1, search });
  const [t] = useTranslation("users");
  const users = data?._embedded?.users;
  const canAddUsers = !!data?._links.create;

  if (data && data.pageTotal < page && page > 1) {
    return <Redirect to={`/users/${data.pageTotal}`} />;
  }

  return (
    <Page
      title={t("users.title")}
      subtitle={t("users.subtitle")}
      loading={isLoading || !users}
      error={error || undefined}
    >
      <UserPage data={data} users={users} page={page} search={search} />
      {canAddUsers ? <CreateButton link="/users/create" label={t("users.createButton")} /> : null}
      <PageActions>
        <OverviewPageActions
          showCreateButton={canAddUsers}
          link="users"
          label={t("users.createButton")}
          searchPlaceholder={t("overview.filterUser")}
        />
      </PageActions>
    </Page>
  );
};

export default Users;
