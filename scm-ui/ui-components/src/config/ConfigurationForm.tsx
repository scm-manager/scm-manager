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
import { ErrorNotification, Level, SubmitButton } from "../index";
import { useTranslation } from "react-i18next";
import Loading from "../Loading";

type Props = {
  isUpdating: boolean;
  isUpdated: boolean;
  isReadOnly: boolean;
  isLoading: boolean;
  isValid: boolean;
  error?: Error | null;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
};

const ConfigurationChangedNotification: FC<{ configChanged: boolean }> = ({ configChanged }) => {
  const [t] = useTranslation("config");
  const [hide, setHide] = useState(false);
  useEffect(() => {
    setHide(false);
  }, [configChanged]);

  if (!configChanged || hide) {
    return null;
  }
  return (
    <div className="notification is-primary">
      <button className="delete" onClick={() => setHide(true)} />
      {t("config.form.submit-success-notification")}
    </div>
  );
};

const ConfigurationForm: FC<Props> = ({
  isLoading,
  isReadOnly,
  isValid,
  isUpdating,
  isUpdated,
  error,
  onSubmit,
  children,
}) => {
  const [t] = useTranslation("config");

  if (isLoading) {
    return <Loading />;
  }

  return (
    <form onSubmit={onSubmit}>
      <ConfigurationChangedNotification configChanged={isUpdated} />
      {children}
      {error && (
        <>
          <hr />
          <ErrorNotification error={error} />
        </>
      )}
      <hr />
      <Level
        right={<SubmitButton label={t("config.form.submit")} disabled={!isValid || isReadOnly} loading={isUpdating} />}
      />
    </form>
  );
};

export default ConfigurationForm;
