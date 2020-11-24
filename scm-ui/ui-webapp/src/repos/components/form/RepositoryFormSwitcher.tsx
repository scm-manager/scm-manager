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
import { Button, ButtonAddons, Level, Subtitle } from "@scm-manager/ui-components";
import { Repository } from "@scm-manager/ui-types";

type Props = {
  repository?: Repository;
  createMode: "CREATE" | "IMPORT";
};

const TopLevel = styled(Level)`
  margin-top: -2rem;
`;

const SmallButton = styled(Button)`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
`;

const RepositoryFormSwitcher: FC<Props> = ({ repository, createMode }) => {
  const [t] = useTranslation("repos");

  const isImportMode = () => {
    return createMode === "IMPORT";
  };

  const isCreateMode = () => {
    return createMode === "CREATE";
  };

  const renderSubtitle = () => {
    let subtitle;
    if (repository) {
      subtitle = "repositoryForm.subtitle";
    } else if (isImportMode()) {
      subtitle = "create.importSubtitle";
    } else {
      subtitle = "create.createSubtitle";
    }

    return <Subtitle subtitle={t(subtitle)} />;
  };

  return (
    <TopLevel
      left={renderSubtitle()}
      right={
        <ButtonAddons>
          <SmallButton
            label={t("create.createButton")}
            icon="fa fa-plus"
            color={isCreateMode() ? "link is-selected" : undefined}
            link={isImportMode() ? "/repos/create" : undefined}
          />
          <SmallButton
            label={t("create.importButton")}
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
