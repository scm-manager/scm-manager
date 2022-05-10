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
  selectedPermissions,
}) => {
  const [t] = useTranslation("permissions");
  const {
    data: availablePermissions,
    error: availablePermissionsLoadError,
    isLoading: isLoadingAvailablePermissions,
  } = useAvailableGlobalPermissions();
  const [permissions, setPermissions] = useState<Record<string, boolean>>({});
  const [permissionsSubmitted, setPermissionsSubmitted] = useState(false);
  const [permissionsChanged, setPermissionsChanged] = useState(false);
  const error = permissionsLoadError || availablePermissionsLoadError;

  useEffect(() => {
    if (selectedPermissions && availablePermissions) {
      const newPermissions: Record<string, boolean> = {};
      availablePermissions.permissions.forEach((p) => (newPermissions[p] = false));
      selectedPermissions.permissions.forEach((p) => (newPermissions[p] = true));
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
      [name]: value,
    });
    setPermissionsChanged(true);
  };

  const onClose = () => setPermissionsSubmitted(false);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (permissions) {
      const selectedPermissions = Object.entries(permissions)
        .filter((e) => e[1])
        .map((e) => e[0]);
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
