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
import { ErrorNotification } from "@scm-manager/ui-components";
import RepositoryForm from "../components/form";
import { Navigate } from "react-router-dom";
import { useCreateRepository } from "@scm-manager/ui-api";

type CreatorComponentProps = {
  repositoryTypes: any;
  namespaceStrategies: any;
  index: any;
};

const CreateRepository: FC<CreatorComponentProps> = ({ repositoryTypes, namespaceStrategies, index }) => {
  const { isPending, error, repository, create } = useCreateRepository();

  if (repository) {
    return <Navigate to={`/repo/${repository.namespace}/${repository.name}/info`} />;
  }

  return (
    <>
      <ErrorNotification error={error} />
      <RepositoryForm
        repositoryTypes={repositoryTypes._embedded.repositoryTypes}
        loading={isPending}
        namespaceStrategy={namespaceStrategies.current}
        createRepository={create}
        indexResources={index}
      />
    </>
  );
};

export default CreateRepository;
