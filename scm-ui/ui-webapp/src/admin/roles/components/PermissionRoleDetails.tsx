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
import { useTranslation } from "react-i18next";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { Level, LinkButton, Title, useDocumentTitle } from "@scm-manager/ui-core";
import PermissionRoleDetailsTable from "./PermissionRoleDetailsTable";

type Props = {
  role: RepositoryRole;
  url: string;
};

const PermissionRoleDetails: FC<Props> = ({ role, url }) => {
  const [t] = useTranslation("admin");
  useDocumentTitle(t("repositoryRole.detailsTitle"));

  const renderEditButton = () => {
    if (!!role._links.update) {
      return (
        <>
          <hr />
          <Level
            right={
              <LinkButton to={`${url}/edit`} variant="primary" color="primary">
                {t("repositoryRole.editButton")}
              </LinkButton>
            }
          />
        </>
      );
    }
    return null;
  };

  return (
    <>
      <Title>{t("repositoryRole.detailsTitle")}</Title>
      <PermissionRoleDetailsTable role={role} />
      {renderEditButton()}
      <ExtensionPoint<extensionPoints.RepositoryRoleDetailsInformation>
        name="repositoryRole.role-details.information"
        renderAll={true}
        props={{
          role,
        }}
      />
    </>
  );
};

export default PermissionRoleDetails;
