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

import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ErrorNotification, Level, Loading, Notification, SubmitButton, Subtitle } from "@scm-manager/ui-components";
import PermissionsWrapper from "./PermissionsWrapper";
import { useAvailableGlobalPermissions } from "@scm-manager/ui-api";
import { GlobalPermissionsCollection } from "@scm-manager/ui-types";

type Props = {
  selectedPermissions?: GlobalPermissionsCollection;
  loadingPermissions?: boolean;
  permissionsLoadError?: Error;
  updatePermissions?: (permissions: string[]) => void;
  isUpdatingPermissions?: boolean;
  permissionsUpdated?: boolean;
  permissionsUpdateError?: Error;
};

const SetPermissions: FC<Props> = ({
  loadingPermissions,
  isUpdatingPermissions,
  permissionsLoadError,
  permissionsUpdateError,
  updatePermissions,
  permissionsUpdated,
  selectedPermissions
}) => {
  const [t] = useTranslation("permissions");
  const {
    data: availablePermissions,
    error: availablePermissionsLoadError,
    isLoading: isLoadingAvailablePermissions
  } = useAvailableGlobalPermissions();
  const [permissions, setPermissions] = useState<Record<string, boolean>>({});
  const [permissionsSubmitted, setPermissionsSubmitted] = useState(false);
  const [permissionsChanged, setPermissionsChanged] = useState(false);
  const error = permissionsLoadError || availablePermissionsLoadError;

  useEffect(() => {
    if (selectedPermissions && availablePermissions) {
      const newPermissions: Record<string, boolean> = {};
      availablePermissions.permissions.forEach(p => (newPermissions[p] = false));
      selectedPermissions.permissions.forEach(p => (newPermissions[p] = true));
      setPermissions(newPermissions);
    }
  }, [availablePermissions, selectedPermissions]);

  useEffect(() => {
    if (permissionsUpdated) {
      setPermissionsSubmitted(true);
      setPermissionsChanged(false);
    }
  }, [permissionsUpdated]);

  if (loadingPermissions || isLoadingAvailablePermissions) {
    return <Loading />;
  }

  if (error) {
    return <ErrorNotification error={error} />;
  }

  const valueChanged = (value: boolean, name: string) => {
    setPermissions({
      ...permissions,
      [name]: value
    });
    setPermissionsChanged(true);
  };

  const onClose = () => setPermissionsSubmitted(false);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (permissions) {
      const selectedPermissions = Object.entries(permissions)
        .filter(e => e[1])
        .map(e => e[0]);
      if (updatePermissions) {
        updatePermissions(selectedPermissions);
      }
    }
  };

  let message = null;

  if (permissionsSubmitted) {
    message = (
      <Notification type={"success"} children={t("setPermissions.setPermissionsSuccessful")} onClose={onClose} />
    );
  } else if (permissionsUpdateError) {
    message = <ErrorNotification error={error} />;
  }

  return (
    <form onSubmit={submit}>
      <Subtitle subtitle={t("setPermissions.subtitle")} />
      {message}
      <PermissionsWrapper permissions={permissions} onChange={valueChanged} disabled={!updatePermissions} />
      <Level
        right={
          <SubmitButton
            disabled={!permissionsChanged}
            loading={isUpdatingPermissions}
            label={t("setPermissions.button")}
            testId="set-permissions-button"
          />
        }
      />
    </form>
  );
};

export default SetPermissions;
