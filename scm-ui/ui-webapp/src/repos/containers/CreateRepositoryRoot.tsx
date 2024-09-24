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
import { Route, Switch } from "react-router-dom";
import CreateRepository from "./CreateRepository";
import ImportRepository from "./ImportRepository";
import { useBinder } from "@scm-manager/ui-extensions";
import { useTranslation } from "react-i18next";
import { Notification, Page, urls } from "@scm-manager/ui-components";
import RepositoryFormSwitcher from "../components/form/RepositoryFormSwitcher";
import { useIndex, useNamespaceStrategies, useRepositoryTypes } from "@scm-manager/ui-api";
import NamespaceAndNameFields from "../components/NamespaceAndNameFields";
import RepositoryInformationForm from "../components/RepositoryInformationForm";
import { extensionPoints } from "@scm-manager/ui-extensions/";

type CreatorRouteProps = {
  creator: extensionPoints.RepositoryCreatorExtension;
  creators: extensionPoints.RepositoryCreatorExtension[];
};

const useCreateRepositoryData = () => {
  const { isLoading: isLoadingNS, error: errorNS, data: namespaceStrategies } = useNamespaceStrategies();
  const { isLoading: isLoadingRT, error: errorRT, data: repositoryTypes } = useRepositoryTypes();
  const { isLoading: isLoadingIdx, error: errorIdx, data: index } = useIndex();
  return {
    isPageLoading: isLoadingNS || isLoadingRT || isLoadingIdx,
    pageLoadingError: errorNS || errorRT || errorIdx || undefined,
    namespaceStrategies,
    repositoryTypes,
    index
  };
};

const CreatorRoute: FC<CreatorRouteProps> = ({ creator, creators }) => {
  const { isPageLoading, pageLoadingError, namespaceStrategies, repositoryTypes, index } = useCreateRepositoryData();
  const [t] = useTranslation(["repos", "plugins"]);

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
      repositoryTypes._embedded.repositoryTypes.length > 0 &&
      index ? (
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

  const creators: extensionPoints.RepositoryCreatorExtension[] = [
    {
      subtitle: t("create.subtitle"),
      path: "",
      icon: "plus",
      label: t("repositoryForm.createButton"),
      component: CreateRepository
    },
    {
      subtitle: t("import.subtitle"),
      path: "import",
      icon: "file-upload",
      label: t("repositoryForm.importButton"),
      component: ImportRepository
    }
  ];

  const extCreators = binder.getExtensions<extensionPoints.RepositoryCreator>("repos.creator");
  if (extCreators) {
    creators.push(...extCreators);
  }

  return (
    <Switch>
      {creators.map(creator => (
        <Route key={creator.path} exact path={urls.concat("/repos/create", creator.path)}>
          <CreatorRoute creator={creator} creators={creators} />
        </Route>
      ))}
    </Switch>
  );
};

export default CreateRepositoryRoot;
