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
  currentUrl: string;
  switchViewLink: string;
};

const CodeViewSwitcher: FC<Props> = ({ currentUrl, switchViewLink }) => {
  const [t] = useTranslation("repos");

  const resolveLocation = () => {
    if (currentUrl.includes("/code/branch") || currentUrl.includes("/code/changesets")) {
      return "changesets";
    }
    if (currentUrl.includes("/code/sources")) {
      return "sources";
    }
    return "";
  };

  const isSourcesTab = () => {
    return resolveLocation() === "sources";
  };

  const isChangesetsTab = () => {
    return resolveLocation() === "changesets";
  };

  return (
    <ButtonAddonsMarginRight>
      <SmallButton
        label={t("code.commits")}
        icon="fa fa-exchange-alt"
        color={isChangesetsTab() ? "link is-selected" : undefined}
        link={isSourcesTab() ? switchViewLink : undefined}
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={isSourcesTab() ? "link is-selected" : undefined}
        link={isChangesetsTab() ? switchViewLink: undefined}
      />
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
