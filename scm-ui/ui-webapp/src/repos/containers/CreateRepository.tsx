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
import { ErrorNotification } from "@scm-manager/ui-components";
import RepositoryForm from "../components/form";
import { Redirect } from "react-router-dom";
import { useCreateRepository } from "@scm-manager/ui-api";
import { CreatorComponentProps } from "../types";

const CreateRepository: FC<CreatorComponentProps> = ({ repositoryTypes, namespaceStrategies, index }) => {
  const { isLoading, error, repository, create } = useCreateRepository();

  if (repository) {
    return <Redirect to={`/repo/${repository.namespace}/${repository.name}`} />;
  }

  return (
    <>
      <ErrorNotification error={error} />
      <RepositoryForm
        repositoryTypes={repositoryTypes._embedded.repositoryTypes}
        loading={isLoading}
        namespaceStrategy={namespaceStrategies.current}
        createRepository={create}
        indexResources={index}
      />
    </>
  );
};

export default CreateRepository;
