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
import { Redirect, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useCreateGroup, urls } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";
import { Page } from "@scm-manager/ui-components";
import GroupForm from "../components/GroupForm";

const CreateGroup: FC = () => {
  const [t] = useTranslation("groups");
  useDocumentTitle(t("addGroup.title"));
  const { isLoading, create, error, group } = useCreateGroup();
  const location = useLocation();

  if (group) {
    return <Redirect to={`/group/${group.name}`} />;
  }

  return (
    <Page title={t("addGroup.title")} subtitle={t("addGroup.subtitle")} error={error || undefined}>
      <div>
        <GroupForm
          submitForm={create}
          loading={isLoading}
          transmittedName={urls.getValueStringFromLocationByKey(location, "name")}
          transmittedExternal={urls.getValueStringFromLocationByKey(location, "external") === "true"}
        />
      </div>
    </Page>
  );
};

export default CreateGroup;
