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
import { Select } from "@scm-manager/ui-components";

type Props = {
  repositoryTypes: RepositoryType[];
  repositoryType?: RepositoryType;
  setRepositoryType: (repositoryType: RepositoryType) => void;
  disabled?: boolean;
};

const ImportRepositoryTypeSelect: FC<Props> = ({ repositoryTypes, repositoryType, setRepositoryType, disabled }) => {
  const [t] = useTranslation("repos");

  const createSelectOptions = () => {
    const options = repositoryTypes
      .filter(repoType => !!repoType._links.import)
      .map(repositoryType => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name
        };
      });
    options.unshift({ label: "", value: "" });
    return options;
  };

  const onChangeType = (type: string) => {
    const repositoryType = repositoryTypes.filter(t => t.name === type)[0];
    setRepositoryType(repositoryType);
  };

  return (
    <Select
      label={t("repository.type")}
      onChange={onChangeType}
      value={repositoryType ? repositoryType.name : ""}
      options={createSelectOptions()}
      helpText={t("help.typeHelpText")}
      disabled={disabled}
    />
  );
};

export default ImportRepositoryTypeSelect;
