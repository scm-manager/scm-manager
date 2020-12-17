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
import { LabelWithHelpIcon, Notification } from "@scm-manager/ui-components";
import SinglePermission from "../containers/SinglePermission";
import { PermissionCollection, RepositoryRole } from "@scm-manager/ui-types";

type Props = {
  availableRepositoryRoles: RepositoryRole[];
  availableVerbs: string[];
  namespace: string;
  repoName?: string;
  permissions: PermissionCollection;
};

const PermissionsTable: FC<Props> = ({
  availableRepositoryRoles,
  availableVerbs,
  namespace,
  repoName,
  permissions,
}) => {
  const [t] = useTranslation("repos");

  if (permissions?.length === 0) {
    return <Notification type="info">{t("permission.noPermissions")}</Notification>;
  }

  return (
    <table className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>
            <LabelWithHelpIcon label={t("permission.name")} helpText={t("permission.help.nameHelpText")} />
          </th>
          <th>
            <LabelWithHelpIcon label={t("permission.role")} helpText={t("permission.help.roleHelpText")} />
          </th>
          <th>
            <LabelWithHelpIcon
              label={t("permission.permissions")}
              helpText={t("permission.help.permissionsHelpText")}
            />
          </th>
          <th />
        </tr>
      </thead>
      <tbody>
        {permissions.map((permission) => {
          return (
            <SinglePermission
              availableRepositoryRoles={availableRepositoryRoles}
              availableRepositoryVerbs={availableVerbs}
              key={permission.name + permission.groupPermission.toString()}
              namespace={namespace}
              repoName={repoName}
              permission={permission}
            />
          );
        })}
      </tbody>
    </table>
  );
};

export default PermissionsTable;
