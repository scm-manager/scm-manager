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
import { Link, RepositoryType } from "@scm-manager/ui-types";
import { LabelWithHelpIcon, Radio } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

type Props = {
  repositoryType: RepositoryType;
  importType: string;
  setImportType: (type: string) => void;
  disabled?: boolean;
};

const RadioGroup = styled.div`
  label {
    margin-right: 2rem;
  }
`;

const ImportTypeSelect: FC<Props> = ({ repositoryType, importType, setImportType, disabled }) => {
  const [t] = useTranslation("repos");

  const changeImportType = (checked: boolean, name?: string) => {
    if (name && checked) {
      setImportType(name);
    }
  };

  return (
    <>
      <LabelWithHelpIcon label={t("import.importTypes.label")} key="import.importTypes.label" />
      <RadioGroup>
        {(repositoryType._links.import as Link[]).map((type, index) => (
          <Radio
            name={type.name}
            checked={importType === type.name}
            value={type.name}
            label={t(`import.importTypes.${type.name}.label`)}
            helpText={t(`import.importTypes.${type.name}.helpText`)}
            onChange={changeImportType}
            key={index}
            disabled={disabled}
          />
        ))}
      </RadioGroup>
    </>
  );
};

export default ImportTypeSelect;
