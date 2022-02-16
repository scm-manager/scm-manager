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
