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

import { Repository } from "@scm-manager/ui-types";
import { ErrorNotification, InputField, Level, Loading, SubmitButton, Notification } from "@scm-manager/ui-components";
import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { HgRepositoryConfiguration, useHgRepositoryConfiguration, useUpdateHgRepositoryConfiguration } from "./hooks";

type Props = {
  repository: Repository;
};

const HgRepositoryConfigurationForm: FC<Props> = ({ repository }) => {
  const [t] = useTranslation("plugins");
  const { isLoading, error, data } = useHgRepositoryConfiguration(repository);
  const { isLoading: isUpdating, error: updateError, update, updated } = useUpdateHgRepositoryConfiguration();
  const [configuration, setConfiguration] = useState<HgRepositoryConfiguration | null>(null);
  useEffect(() => {
    setConfiguration(data);
  }, [data]);

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading || !configuration) {
    return <Loading />;
  }

  const encodingChanged = (value: string) => {
    const encoding = value ? value : undefined;
    setConfiguration({
      ...configuration,
      encoding,
    });
  };

  const submit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    update(configuration);
  };

  const readOnly = !configuration._links.update;

  return (
    <form onSubmit={submit}>
      <ErrorNotification error={updateError} />
      {updated ? <Notification type="info">{t("scm-hg-plugin.config.success")}</Notification> : null}
      <InputField
        name="encoding"
        label={t("scm-hg-plugin.config.encoding.label")}
        value={configuration.encoding || ""}
        onChange={encodingChanged}
        disabled={readOnly}
      />
      {!readOnly ? (
        <Level right={<SubmitButton loading={isUpdating} label={t("scm-hg-plugin.config.submit")} />} />
      ) : null}
    </form>
  );
};

export default HgRepositoryConfigurationForm;
