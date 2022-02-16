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
import React, { FC, ReactNode } from "react";
import styled from "styled-components";
import { useLocation } from "react-router-dom";
import { BranchSelector, devices, Level } from "@scm-manager/ui-components";
import CodeViewSwitcher, { SwitchViewLink } from "./CodeViewSwitcher";
import { useTranslation } from "react-i18next";
import { Branch } from "@scm-manager/ui-types";

const ActionBar = styled.div`
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
  margin-bottom: 1em;
  @media screen and (max-width: ${devices.tablet.width}px) {
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
  @media screen and (max-width: ${devices.tablet.width}px) {
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
        right={<CodeViewSwitcher currentUrl={location.pathname} switchViewLink={switchViewLink} />}
      />
    </ActionBar>
  );
};

export default CodeActionBar;
