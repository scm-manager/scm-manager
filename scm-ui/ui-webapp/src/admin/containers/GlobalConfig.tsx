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
import { Link } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Title } from "@scm-manager/ui-components";
import ConfigForm from "../components/form/ConfigForm";
import { useConfig, useIndexLinks, useNamespaceStrategies, useUpdateConfig } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";

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
  useDocumentTitle(t("config.title"));
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
