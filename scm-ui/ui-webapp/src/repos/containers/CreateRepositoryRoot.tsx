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
    index,
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
    <Switch>
      {creators.map((creator) => (
        <Route key={creator.path} exact path={urls.concat("/repos/create", creator.path)}>
          <CreatorRoute creator={creator} creators={creators} />
        </Route>
      ))}
    </Switch>
  );
};

export default CreateRepositoryRoot;
