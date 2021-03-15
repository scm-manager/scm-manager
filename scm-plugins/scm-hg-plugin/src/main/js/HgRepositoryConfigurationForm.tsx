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
      encoding
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
        label={t("scm-hg-plugin.config.encoding")}
        helpText={t("scm-hg-plugin.config.encodingHelpText")}
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
