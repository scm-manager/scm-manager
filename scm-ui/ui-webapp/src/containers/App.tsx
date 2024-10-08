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
import Main from "./Main";
import { useHistory } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { useIndex, useSubject } from "@scm-manager/ui-api";
import { ErrorPage, Footer, Header, Loading } from "@scm-manager/ui-components";
import { binder } from "@scm-manager/ui-extensions";
import usePauseShortcutsWhenModalsActive from "../shortcuts/usePauseShortcutsWhenModalsActive";
import { useShortcut } from "@scm-manager/ui-shortcuts";
import Login from "./Login";
import NavigationBar from "./NavigationBar";
import styled from "styled-components";
import ShortcutDocsModal from "../shortcuts/ShortcutDocsModal";

const AppWrapper = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
`;

const App: FC = () => {
  const { data: index } = useIndex();
  const { isLoading, error, isAuthenticated, isAnonymous, me } = useSubject();
  const [t] = useTranslation("commons");
  usePauseShortcutsWhenModalsActive();

  const history = useHistory();

  useShortcut("option+r", () => history.push("/repos/"), {
    active: !!index?._links["repositories"],
    description: t("shortcuts.repositories"),
  });
  useShortcut("option+u", () => history.push("/users/"), {
    active: !!index?._links["users"],
    description: t("shortcuts.users"),
  });
  useShortcut("option+g", () => history.push("/groups/"), {
    active: !!index?._links["groups"],
    description: t("shortcuts.groups"),
  });
  useShortcut("option+a", () => history.push("/admin/"), {
    active: !!index?._links["config"],
    description: t("shortcuts.admin"),
  });

  if (!index) {
    return null;
  }

  let content;

  // authenticatedOrAnonymous means authorized, we stick on authenticatedOrAnonymous for compatibility reasons
  const authenticatedOrAnonymous = isAuthenticated || isAnonymous;

  if (index?.initialization) {
    const Extension = binder.getExtension(`initialization.step.${index.initialization}`);
    content = <Extension data={index?._embedded ? index._embedded[index.initialization] : undefined} />;
  } else if (!authenticatedOrAnonymous && !isLoading) {
    content = <Login />;
  } else if (isLoading) {
    content = <Loading />;
  } else if (error) {
    content = <ErrorPage title={t("app.error.title")} subtitle={t("app.error.subtitle")} error={error} />;
  } else if (me) {
    content = <Main authenticated={authenticatedOrAnonymous} me={me} links={index._links} />;
  }

  return (
    <AppWrapper className={classNames("App", { "has-navbar-fixed-top": authenticatedOrAnonymous })}>
      <Header authenticated={authenticatedOrAnonymous}>
        <NavigationBar links={index._links} />
      </Header>
      <ShortcutDocsModal />
      <div className="is-flex-grow-1">{content}</div>
      <Footer me={me} version={index.version} links={index._links} />
    </AppWrapper>
  );
};

export default App;
