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
