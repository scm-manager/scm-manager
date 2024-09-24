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

import React, { FC } from "react";
import styled from "styled-components";
import { Button, ButtonAddons } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";

const SmallButton = styled(Button)`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  padding-left: 0.75rem;
  padding-right: 0.75rem;
`;

const ButtonAddonsMarginRight = styled(ButtonAddons)`
  margin-right: -0.2em;
`;

type Type = "sources" | "changesets";

export type SwitchViewLink = string | ((type: Type) => string);

type Props = {
  currentUrl: string;
  switchViewLink: SwitchViewLink;
};

const CodeViewSwitcher: FC<Props> = ({ currentUrl, switchViewLink }) => {
  const { t } = useTranslation("repos");

  let location = "";
  if (currentUrl.includes("/code/branch") || currentUrl.includes("/code/changesets")) {
    location = "changesets";
  } else if (currentUrl.includes("/code/sources")) {
    location = "sources";
  } else if (currentUrl.includes("/code/search")) {
    location = "search";
  }

  const createLink = (type: Type) => {
    if (typeof switchViewLink === "string") {
      return switchViewLink;
    }
    return switchViewLink(type);
  };

  return (
    <ButtonAddonsMarginRight>
      <SmallButton
        label={t("code.commits")}
        icon="fa fa-exchange-alt"
        color={location === "changesets" ? "link is-selected" : undefined}
        link={location !== "changesets" ? createLink("changesets") : undefined}
      />
      <SmallButton
        label={t("code.sources")}
        icon="fa fa-code"
        color={location === "sources" || location === "search" ? "link is-selected" : undefined}
        link={location !== "sources" ? createLink("sources") : undefined}
      />
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
