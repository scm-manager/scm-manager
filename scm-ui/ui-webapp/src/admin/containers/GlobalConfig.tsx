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
import { Link } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Title } from "@scm-manager/ui-components";
import ConfigForm from "../components/form/ConfigForm";
import { useConfig, useIndexLinks, useNamespaceStrategies, useUpdateConfig } from "@scm-manager/ui-api";

const GlobalConfig: FC = () => {
  const indexLinks = useIndexLinks();
  const { data: config, error: configLoadingError, isLoading: isLoadingConfig } = useConfig();
  const { isLoading: isUpdating, error: updateError, isUpdated, update, reset } = useUpdateConfig();
  const {
    data: namespaceStrategies,
    error: namespaceStrategiesLoadingError,
    isLoading: isLoadingNamespaceStrategies,
  } = useNamespaceStrategies();
  const [t] = useTranslation("config");
  const error = configLoadingError || namespaceStrategiesLoadingError || updateError || undefined;
  const isLoading = isLoadingNamespaceStrategies || isLoadingConfig;
  const canUpdateConfig = !!(config && (config._links.update as Link).href);

  if (isLoading) {
    return <Loading />;
  }

  const renderConfigChangedNotification = () => {
    if (isUpdated) {
      return (
        <div className="notification is-primary">
          <button className="delete" onClick={reset} />
          {t("config.form.submit-success-notification")}
        </div>
      );
    }
    return null;
  };

  const renderContent = () => (
    <>
      {renderConfigChangedNotification()}
      <ConfigForm
        submitForm={update}
        config={config}
        loading={isUpdating}
        namespaceStrategies={namespaceStrategies}
        configUpdatePermission={canUpdateConfig}
        configReadPermission={!!config}
        invalidateCachesLink={indexLinks.invalidateCaches as Link | undefined}
        invalidateSearchIndexLink={indexLinks.invalidateSearchIndex as Link | undefined}
      />
    </>
  );

  return (
    <>
      <Title title={t("config.title")} />
      {error ? <ErrorNotification error={error} /> : renderContent()}
    </>
  );
};

export default GlobalConfig;
