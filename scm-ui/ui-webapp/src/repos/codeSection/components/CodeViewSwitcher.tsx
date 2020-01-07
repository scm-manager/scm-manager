import React, { FC } from "react";
import styled from "styled-components";
import Button from "@scm-manager/ui-components/src/buttons/Button";
import ButtonAddons from "@scm-manager/ui-components/src/buttons/ButtonAddons";
import { useTranslation } from "react-i18next";

const SmallButton = styled(Button).attrs(() => ({}))`
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
`;

type Props = {
  url: string;
};

const CodeViewSwitcher: FC<Props> = ({ url }) => {
  const [t] = useTranslation("repos");

  return (
    <ButtonAddons>
      <SmallButton
        label={t("code.commits")}
        icon="fa fa-exchange-alt"
        color={url.includes("/code/changeset") ? "link is-selected" : undefined}
        link={url.replace("/code/sources", "/code/changesets")}
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={url.includes("/code/sources") ? "link is-selected" : undefined}
        link={url.replace("/code/changesets", "/code/sources")}
      />
    </ButtonAddons>
  );
};

export default CodeViewSwitcher;
