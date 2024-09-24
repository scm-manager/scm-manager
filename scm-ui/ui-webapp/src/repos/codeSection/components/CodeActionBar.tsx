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

import React, { FC, ReactNode } from "react";
import styled from "styled-components";
import { useLocation } from "react-router-dom";
import { BranchSelector, devices, Level, urls } from "@scm-manager/ui-components";
import CodeViewSwitcher, { SwitchViewLink } from "./CodeViewSwitcher";
import { useTranslation } from "react-i18next";
import { Branch } from "@scm-manager/ui-types";

const ActionBar = styled.div`
  border: 1px solid var(--scm-border-color);
  border-radius: 4px;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
  margin-bottom: 1em;
  @media screen and (max-width: ${devices.tablet.width}-1px) {
    padding: 0.5rem 0.25rem;
  }
`;

const FlexShrinkLevel = styled(Level)`
  .level-left {
    flex-shrink: 1;
    margin-right: 0.75rem;
  }
  .level-item {
    justify-content: flex-end;
  }
  @media screen and (max-width: ${devices.tablet.width}-1px) {
    .level-left {
      margin-right: 0;
    }
    .level-item {
      margin-top: 0.5rem;
    }
  }
`;

type Props = {
  selectedBranch?: string;
  branches?: Branch[];
  onSelectBranch: () => void;
  switchViewLink: SwitchViewLink;
  actions?: ReactNode;
};

const CodeActionBar: FC<Props> = ({ selectedBranch, branches, onSelectBranch, switchViewLink, actions }) => {
  const { t } = useTranslation("repos");
  const location = useLocation();

  return (
    <ActionBar className="has-background-secondary-less">
      <FlexShrinkLevel
        left={
          branches &&
          branches?.length > 0 && (
            <BranchSelector
              label={t("code.branchSelector")}
              branches={branches}
              selectedBranch={selectedBranch}
              onSelectBranch={onSelectBranch}
            />
          )
        }
        children={actions}
        right={
          <CodeViewSwitcher currentUrl={urls.escapeUrlForRoute(location.pathname)} switchViewLink={switchViewLink} />
        }
      />
    </ActionBar>
  );
};

export default CodeActionBar;
