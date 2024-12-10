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
import { Group } from "@scm-manager/ui-types";
import { useGroupPermissions, useSetGroupPermissions } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";
import SetPermissions from "./SetPermissions";

type Props = {
  group: Group;
};

const SetGroupPermissions: FC<Props> = ({ group }) => {
  const [t] = useTranslation("groups");
  useDocumentTitle(t("singleGroup.menu.setPermissionsNavLink"), group.name);
  const {
    data: selectedPermissions,
    isLoading: loadingPermissions,
    error: permissionsLoadError,
  } = useGroupPermissions(group);
  const {
    isLoading: isUpdatingPermissions,
    isUpdated: permissionsUpdated,
    setPermissions,
    error: permissionsUpdateError,
  } = useSetGroupPermissions(group, selectedPermissions);
  return (
    <SetPermissions
      selectedPermissions={selectedPermissions}
      loadingPermissions={loadingPermissions}
      isUpdatingPermissions={isUpdatingPermissions}
      permissionsLoadError={permissionsLoadError || undefined}
      permissionsUpdated={permissionsUpdated}
      updatePermissions={setPermissions}
      permissionsUpdateError={permissionsUpdateError || undefined}
    />
  );
};

export default SetGroupPermissions;
