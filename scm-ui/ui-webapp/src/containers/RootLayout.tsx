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
import classNames from "classnames";
import { Footer, Header } from "@scm-manager/ui-components";
import NavigationBar from "./NavigationBar";
import ShortcutDocsModal from "../shortcuts/ShortcutDocsModal";
import styled from "styled-components";
import { useIndex, useMe } from "@scm-manager/ui-api";
import { Outlet, useNavigate } from "react-router-dom";
import { useShortcut } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import usePauseShortcutsWhenModalsActive from "../shortcuts/usePauseShortcutsWhenModalsActive";
import { useCodeSettingsConfig } from "../codeSettingsConfig";

const AppWrapper = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
`;

export const RootLayout: FC = () => {
  const authenticatedOrAnonymous = true;
  const index = useIndex();
  const { data: me } = useMe();
  const [t] = useTranslation("commons");

  const navigate = useNavigate();

  const {
    value: { tabSize },
  } = useCodeSettingsConfig();

  usePauseShortcutsWhenModalsActive();

  useShortcut("option+r", () => navigate("/repos/"), {
    active: !!index?._links["repositories"],
    description: t("shortcuts.repositories"),
  });

  useShortcut("option+u", () => navigate("/users/"), {
    active: !!index?._links["users"],
    description: t("shortcuts.users"),
  });

  useShortcut("option+g", () => navigate("/groups/"), {
    active: !!index?._links["groups"],
    description: t("shortcuts.groups"),
  });

  useShortcut("option+a", () => navigate("/admin/"), {
    active: !!index?._links["config"],
    description: t("shortcuts.admin"),
  });

  if (!index) {
    return null;
  }

  return (
    <AppWrapper
      style={{ "--user-tab-size": tabSize } as React.CSSProperties}
      className={classNames("App", { "has-navbar-fixed-top": authenticatedOrAnonymous })}
    >
      <Header authenticated={authenticatedOrAnonymous}>
        <NavigationBar links={index._links} />
      </Header>
      <ShortcutDocsModal />
      <div className="is-flex-grow-1">
        <Outlet />
      </div>
      <Footer me={me} version={index.version} links={index._links} />
    </AppWrapper>
  );
};
