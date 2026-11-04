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
import { Help } from "@scm-manager/ui-components";
import { Notification } from "@scm-manager/ui-core";
import SinglePermission from "../containers/SinglePermission";
import { Namespace, PermissionCollection, Repository, RepositoryRole } from "@scm-manager/ui-types";

type Props = {
  availableRoles: RepositoryRole[];
  availableVerbs: string[];
  permissions: PermissionCollection;
  namespaceOrRepository: Namespace | Repository;
};

const PermissionsTable: FC<Props> = ({
  availableRoles,
  availableVerbs,
  namespaceOrRepository,
  permissions: permissionCollection
}) => {
  const [t] = useTranslation("repos");

  if (permissionCollection._embedded.permissions?.length === 0) {
    return <Notification type="info">{t("permission.noPermissions")}</Notification>;
  }

  permissionCollection?._embedded.permissions.sort((a, b) => {
    if (a.name > b.name) {
      return 1;
    } else if (a.name < b.name) {
      return -1;
    } else {
      return 0;
    }
  });

  return (
    <div className="is-overflow-x-auto">
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("permission.name")}</th>
            <th>
              {t("permission.role")}
              <Help message={t("permission.help.roleHelpText")} />
            </th>
            <th>
              {t("permission.permissions")}
              <Help message={t("permission.help.permissionsHelpText")} />
            </th>
            <th />
          </tr>
        </thead>
        <tbody>
          {permissionCollection?._embedded.permissions.map(permission => {
            return (
              <SinglePermission
                availableRoles={availableRoles}
                availableVerbs={availableVerbs}
                key={permission.name + permission.groupPermission.toString()}
                namespaceOrRepository={namespaceOrRepository}
                permission={permission}
              />
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default PermissionsTable;
