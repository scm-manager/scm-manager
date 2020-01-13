import React, { FC } from "react";
import styled from "styled-components";
import { Button, ButtonAddons } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Branch } from "@scm-manager/ui-types";

const SmallButton = styled(Button).attrs(() => ({}))`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
`;

const ButtonAddonsMarginRight = styled(ButtonAddons)`
  margin-right: -0.2em;
`;

type Props = {
  baseUrl: string;
  currentUrl: string;
  branches: Branch[];
  selectedBranch: string;
};

const CodeViewSwitcher: FC<Props> = ({ baseUrl, currentUrl, branches, selectedBranch }) => {
  const [t] = useTranslation("repos");

  const createDestinationUrl = (destination: string, branch?: string, suffix?: string) => {
    if (!branches) {
      return baseUrl + "/" + destination + "/";
    }
    let splittedUrl = currentUrl.split("/");
    splittedUrl[5] = destination;
    if (branch) {
      splittedUrl[6] = branch;
    }
    splittedUrl.splice(7, splittedUrl.length);
    if (suffix) {
      splittedUrl.push(suffix);
    }
    return splittedUrl.join("/");
  };

  return (
    <ButtonAddonsMarginRight>
      <SmallButton
        label={t("code.commits")}
        icon="fa fa-exchange-alt"
        color={
          currentUrl.includes("/code/branch") || currentUrl.includes("/code/changesets")
            ? "link is-selected"
            : undefined
        }
        link={
          branches
            ? branches.filter(branch => branch.name === selectedBranch).length === 0
              ? createDestinationUrl(
                  "branch",
                  branches.filter(branch => branch.defaultBranch === true)[0].name,
                  "changesets/"
                )
              : createDestinationUrl("branch", selectedBranch, "changesets/")
            : createDestinationUrl("changesets")
        }
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={currentUrl.includes("/code/sources") ? "link is-selected" : undefined}
        link={createDestinationUrl("sources")}
      />
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
