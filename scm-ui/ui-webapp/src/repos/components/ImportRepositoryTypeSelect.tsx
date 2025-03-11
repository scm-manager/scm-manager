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
import { RepositoryType } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { Label, Loading, Select } from "@scm-manager/ui-core";

type Props = {
  repositoryTypes: RepositoryType[];
  repositoryType?: RepositoryType;
  setRepositoryType: (repositoryType: RepositoryType) => void;
  disabled?: boolean;
};

const ImportRepositoryTypeSelect: FC<Props> = ({ repositoryTypes, repositoryType, setRepositoryType, disabled }) => {
  const [t] = useTranslation("repos");

  const createSelectOptions = () => {
    return repositoryTypes
      .filter((repoType) => !!repoType._links.import)
      .map((repositoryType) => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name,
        };
      });
  };

  const onChangeType = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const type = event.target.value;
    const repositoryType = repositoryTypes.filter((t) => t.name === type)[0];
    setRepositoryType(repositoryType);
  };

  if (!repositoryType) {
    return <Loading />;
  }

  return (
    <Label className="is-flex is-align-items-baseline">
      <span className="mr-2">{t("repository.type")}</span>
      <Select
        onChange={onChangeType}
        options={createSelectOptions()}
        disabled={disabled}
        defaultValue={repositoryType.name}
      />
    </Label>
  );
};

export default ImportRepositoryTypeSelect;
