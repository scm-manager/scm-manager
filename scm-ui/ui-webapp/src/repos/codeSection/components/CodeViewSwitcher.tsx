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
  url: string;
  branches: Branch[];
};

const CodeViewSwitcher: FC<Props> = ({ url, branches }) => {
  const [t] = useTranslation("repos");

  const createDestinationUrl = (destination: string, suffix?: string) => {
    let splittedUrl = url.split("/");
    splittedUrl[5] = destination;
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
        color={url.includes("/code/branch/") || url.includes("/code/changesets/") ? "link is-selected" : undefined}
        link={branches ? createDestinationUrl("branch", "changesets/") : createDestinationUrl("changesets")}
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={url.includes("/code/sources") ? "link is-selected" : undefined}
        link={createDestinationUrl("sources")}
      />
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
