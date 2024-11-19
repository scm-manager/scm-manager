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
import { Link, RepositoryType } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { RadioGroup, RadioGroupField } from "@scm-manager/ui-core";

type Props = {
  repositoryType: RepositoryType;
  importType: string;
  setImportType: (type: string) => void;
  disabled?: boolean;
};

const ImportTypeSelect: FC<Props> = ({ repositoryType, importType, setImportType, disabled }) => {
  const [t] = useTranslation("repos");

  return (
    <RadioGroupField label={t("import.importTypes.label")} onValueChange={setImportType} disabled={disabled}>
      {(repositoryType._links.import as Link[]).map((type) => (
        <RadioGroup.Option
          checked={importType === type.name}
          value={type.name || ""}
          label={t(`import.importTypes.${type.name}.label`)}
          helpText={t(`import.importTypes.${type.name}.helpText`)}
          key={type.name}
        />
      ))}
    </RadioGroupField>
  );
};

export default ImportTypeSelect;
