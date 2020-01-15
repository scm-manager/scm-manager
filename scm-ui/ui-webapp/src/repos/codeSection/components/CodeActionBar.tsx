import React, { FC } from "react";
import styled from "styled-components";
import { useLocation } from "react-router-dom";
import { Level, BranchSelector } from "@scm-manager/ui-components";
import CodeViewSwitcher from "./CodeViewSwitcher";
import { useTranslation } from "react-i18next";
import { Branch } from "@scm-manager/ui-types";

const ActionBar = styled.div`
  background-color: whitesmoke;
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  color: #363636;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
  margin-bottom: 1em;
`;

type Props = {
  selectedBranch?: string;
  branches: Branch[];
  onSelectBranch: () => void;
  switchViewLink: string;
};

const CodeActionBar: FC<Props> = ({ selectedBranch, branches, onSelectBranch, switchViewLink }) => {
  const { t } = useTranslation("repos");
  const location = useLocation();

  return (
    <ActionBar>
      <Level
        left={
          branches?.length > 0 && (
            <BranchSelector
              label={t("code.branchSelector")}
              branches={branches}
              selectedBranch={selectedBranch}
              onSelectBranch={onSelectBranch}
            />
          )
        }
        right={<CodeViewSwitcher currentUrl={location.pathname} switchViewLink={switchViewLink} />}
      />
    </ActionBar>
  );
};

export default CodeActionBar;
