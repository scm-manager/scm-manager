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
import styled, { css } from "styled-components";
import { Button, Icon, LinkButton } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";

const smallButtonStyles = css`
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 600;
  padding-left: 0.75rem;
  padding-right: 0.75rem;
`;

const SmallButton = styled(Button)`
  ${smallButtonStyles}
`;

const SmallLinkButton = styled(LinkButton)`
  ${smallButtonStyles}
`;

const ButtonAddonsMarginRight = styled.div`
  margin-right: -0.2em;
`;

type Type = "sources" | "changesets";

export type SwitchViewLink = string | ((type: Type) => string);

type Props = {
  currentUrl: string;
  switchViewLink: SwitchViewLink;
};

type SwitchButtonProps = {
  icon: string;
  label: string;
  selected: boolean;
  link?: string;
};

const SwitchButton: FC<SwitchButtonProps> = ({ icon, label, selected, link }) => {
  const className = selected ? "is-link is-selected" : undefined;
  const content = (
    <>
      <Icon>{icon}</Icon>
      <span>{label}</span>
    </>
  );
  if (link) {
    return (
      <SmallLinkButton className={className} to={link}>
        {content}
      </SmallLinkButton>
    );
  }
  return <SmallButton className={className}>{content}</SmallButton>;
};

const CodeViewSwitcher: FC<Props> = ({ currentUrl, switchViewLink }) => {
  const { t } = useTranslation("repos");

  let location = "";
  if (
    currentUrl.includes("/code/branch") ||
    currentUrl.includes("/code/tag") ||
    currentUrl.includes("/code/revision") ||
    currentUrl.includes("/code/changesets")
  ) {
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
    <ButtonAddonsMarginRight className="field has-addons mb-0">
      <p className="control">
        <SwitchButton
          icon="exchange-alt"
          label={t("code.commits")}
          selected={location === "changesets"}
          link={location !== "changesets" ? createLink("changesets") : undefined}
        />
      </p>
      <p className="control">
        <SwitchButton
          icon="code"
          label={t("code.sources")}
          selected={location === "sources" || location === "search"}
          link={location !== "sources" ? createLink("sources") : undefined}
        />
      </p>
    </ButtonAddonsMarginRight>
  );
};

export default CodeViewSwitcher;
