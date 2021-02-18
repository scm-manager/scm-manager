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
import { Page } from "@scm-manager/ui-components";
import RepositoryForm from "../components/form";
import RepositoryFormSwitcher from "../components/form/RepositoryFormSwitcher";
import { Redirect } from "react-router-dom";
import { useCreateRepository, useIndex, useNamespaceStrategies, useRepositoryTypes } from "@scm-manager/ui-api";

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

const CreateRepository: FC = () => {
  const { isPageLoading, pageLoadingError, namespaceStrategies, repositoryTypes, index } = useCreateRepositoryData();
  const { isLoading, error, repository, create } = useCreateRepository();
  const [t] = useTranslation("repos");

  if (repository) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}`} />;
  }

  return (
    <Page
      title={t("create.title")}
      subtitle={t("create.subtitle")}
      afterTitle={<RepositoryFormSwitcher creationMode={"CREATE"} />}
      loading={isPageLoading}
      error={pageLoadingError || error || undefined}
      showContentOnError={true}
    >
      {namespaceStrategies && repositoryTypes ? (
        <RepositoryForm
          repositoryTypes={repositoryTypes._embedded.repositoryTypes}
          loading={isLoading}
          namespaceStrategy={namespaceStrategies!.current}
          createRepository={create}
          indexResources={index}
        />
      ) : null}
    </Page>
  );
};

export default CreateRepository;
