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
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import { Button, ButtonAddons, Level } from "@scm-manager/ui-components";

type Props = {
  creationMode: "CREATE" | "IMPORT";
};

const SmallButton = styled(Button)`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
`;

const TopLevel = styled(Level)`
  margin-top: -2.75rem;
  margin-bottom: 2.75rem !important; //TODO Try to remove important
  height: 0;
`;

const RepositoryFormSwitcher: FC<Props> = ({ creationMode }) => {
  const [t] = useTranslation("repos");

  const isImportMode = () => {
    return creationMode === "IMPORT";
  };

  const isCreateMode = () => {
    return creationMode === "CREATE";
  };

  return (
    <TopLevel
      right={
        <ButtonAddons>
          <SmallButton
            label={t("repositoryForm.createButton")}
            icon="fa fa-plus"
            color={isCreateMode() ? "link is-selected" : undefined}
            link={isImportMode() ? "/repos/create" : undefined}
          />
          <SmallButton
            label={t("repositoryForm.importButton")}
            icon="fa fa-file-upload"
            color={isImportMode() ? "link is-selected" : undefined}
            link={isCreateMode() ? "/repos/import" : undefined}
          />
        </ButtonAddons>
      }
    />
  );
};

export default RepositoryFormSwitcher;
