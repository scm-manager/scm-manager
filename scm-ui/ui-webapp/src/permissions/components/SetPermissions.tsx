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
import { Link } from "@scm-manager/ui-types";
import { ErrorNotification, Level, Notification, SubmitButton, Subtitle } from "@scm-manager/ui-components";
import { loadPermissionsForEntity, setPermissions as updatePermissions } from "./handlePermissions";
import PermissionsWrapper from "./PermissionsWrapper";
import { useRequiredIndexLink } from "@scm-manager/ui-api";

type Props = {
  selectedPermissionsLink: Link;
};

const SetPermissions: FC<Props> = ({ selectedPermissionsLink }) => {
  const [t] = useTranslation("permissions");
  const availablePermissionLink = useRequiredIndexLink("permissions");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null | undefined>();
  const [permissionsSubmitted, setPermissionsSubmitted] = useState(false);
  const [permissionsChanged, setPermissionsChanged] = useState(false);
  const [overwritePermissionsLink, setOverwritePermissionsLink] = useState<Link | undefined>();
  const [permissions, setPermissions] = useState<{
    [key: string]: boolean;
  }>({});

  useEffect(() => {
    loadPermissionsForEntity(availablePermissionLink, selectedPermissionsLink.href).then(response => {
      const { permissions, overwriteLink } = response;
      setPermissions(permissions);
      setOverwritePermissionsLink(overwriteLink);
      setLoading(false);
    });
  }, [availablePermissionLink, selectedPermissionsLink]);

  const setLoadingState = () => setLoading(true);

  const setErrorState = (error: Error) => {
    setLoading(false);
    setError(error);
  };

  const setSuccessfulState = () => {
    setLoading(false);
    setError(undefined);
    setPermissionsSubmitted(true);
    setPermissionsChanged(false);
  };

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
      setLoadingState();
      const selectedPermissions = Object.entries(permissions)
        .filter(e => e[1])
        .map(e => e[0]);
      if (overwritePermissionsLink) {
        updatePermissions(overwritePermissionsLink.href, selectedPermissions)
          .then(_ => setSuccessfulState())
          .catch(err => setErrorState(err));
      }
    }
  };

  let message = null;

  if (permissionsSubmitted) {
    message = (
      <Notification type={"success"} children={t("setPermissions.setPermissionsSuccessful")} onClose={onClose} />
    );
  } else if (error) {
    message = <ErrorNotification error={error} />;
  }

  return (
    <form onSubmit={submit}>
      <Subtitle subtitle={t("setPermissions.subtitle")} />
      {message}
      <PermissionsWrapper permissions={permissions} onChange={valueChanged} disabled={!overwritePermissionsLink} />
      <Level
        right={
          <SubmitButton
            disabled={!permissionsChanged}
            loading={loading}
            label={t("setPermissions.button")}
            testId="set-permissions-button"
          />
        }
      />
    </form>
  );
};
export default SetPermissions;
