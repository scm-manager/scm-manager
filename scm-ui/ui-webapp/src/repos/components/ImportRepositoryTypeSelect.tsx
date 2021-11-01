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
      .filter((repoType) => !!repoType._links.import)
      .map((repositoryType) => {
        return {
          label: repositoryType.displayName,
          value: repositoryType.name,
        };
      });
    options.unshift({ label: "", value: "" });
    return options;
  };

  const onChangeType = (type: string) => {
    const repositoryType = repositoryTypes.filter((t) => t.name === type)[0];
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
