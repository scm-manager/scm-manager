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
import { Route, Routes } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useIndex, useNamespaceStrategies, useRepositoryTypes } from "@scm-manager/ui-api";
import { Page } from "@scm-manager/ui-components";
import { Notification, useDocumentTitle } from "@scm-manager/ui-core";
import { extensionPoints, useBinder } from "@scm-manager/ui-extensions";
import NamespaceAndNameFields from "../components/NamespaceAndNameFields";
import RepositoryInformationForm from "../components/RepositoryInformationForm";
import RepositoryFormSwitcher from "../components/form/RepositoryFormSwitcher";
import CreateRepository from "./CreateRepository";
import ImportRepository from "./ImportRepository";

type CreatorRouteProps = {
  creator: extensionPoints.RepositoryCreator["type"];
  creators: extensionPoints.RepositoryCreator["type"][];
};

const useCreateRepositoryData = () => {
  const { isLoading: isLoadingNS, error: errorNS, data: namespaceStrategies } = useNamespaceStrategies();
  const { isLoading: isLoadingRT, error: errorRT, data: repositoryTypes } = useRepositoryTypes();
  const index = useIndex();
  return {
    isPageLoading: isLoadingNS || isLoadingRT,
    pageLoadingError: errorNS || errorRT || undefined,
    namespaceStrategies,
    repositoryTypes,
    index,
  };
};

const CreatorRoute: FC<CreatorRouteProps> = ({ creator, creators }) => {
  const { isPageLoading, pageLoadingError, namespaceStrategies, repositoryTypes, index } = useCreateRepositoryData();
  const [t] = useTranslation(["repos", "plugins"]);
  useDocumentTitle(t(`plugins:${creator.subtitle}`, creator.subtitle));

  const Component = creator.component;

  return (
    <Page
      title={t("repos:create.title")}
      subtitle={t(`plugins:${creator.subtitle}`, creator.subtitle)}
      afterTitle={<RepositoryFormSwitcher forms={creators} />}
      loading={isPageLoading}
      error={pageLoadingError}
    >
      {namespaceStrategies &&
      repositoryTypes &&
      repositoryTypes?._embedded?.repositoryTypes &&
      repositoryTypes._embedded.repositoryTypes.length > 0 ? (
        <Component
          namespaceStrategies={namespaceStrategies}
          repositoryTypes={repositoryTypes}
          index={index}
          nameForm={NamespaceAndNameFields}
          informationForm={RepositoryInformationForm}
        />
      ) : (
        <Notification type="warning">{t("create.noTypes")}</Notification>
      )}
    </Page>
  );
};

const CreateRepositoryRoot: FC = () => {
  const [t] = useTranslation("repos");
  const binder = useBinder();

  const creators: extensionPoints.RepositoryCreator["type"][] = [
    {
      subtitle: t("create.subtitle"),
      path: "",
      icon: "plus",
      label: t("repositoryForm.createButton"),
      component: CreateRepository,
    },
    {
      subtitle: t("import.subtitle"),
      path: "import",
      icon: "file-upload",
      label: t("repositoryForm.importButton"),
      component: ImportRepository,
    },
  ];

  const extCreators = binder.getExtensions<extensionPoints.RepositoryCreator>("repos.creator");
  if (extCreators) {
    creators.push(...extCreators);
  }

  return (
    <Routes>
      {creators.map((creator) =>
        creator.path ? (
          <Route
            key={creator.path}
            path={creator.path}
            element={<CreatorRoute creator={creator} creators={creators} />}
          />
        ) : (
          <Route key="index" index element={<CreatorRoute creator={creator} creators={creators} />} />
        ),
      )}
    </Routes>
  );
};

export default CreateRepositoryRoot;
