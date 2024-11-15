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

import React, { FC, useEffect, useRef, useState } from "react";
import { Links } from "@scm-manager/ui-types";
import classNames from "classnames";
import styled from "styled-components";
import { devices, Logo, PrimaryNavigation } from "@scm-manager/ui-components";
import Notifications from "./Notifications";
import OmniSearch from "./OmniSearch";
import LogoutButton from "./LogoutButton";
import LoginButton from "./LoginButton";
import { useTranslation } from "react-i18next";
import Alerts from "./Alerts";

const StyledMenuBar = styled.div`
  background-color: transparent !important;
`;

const LogoItem = styled.a`
  cursor: default !important;
`;

const StyledNavBar = styled.nav`
  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    .navbar-header-actions {
      position: absolute;
      top: 0;
      left: 52px;
      flex-direction: row-reverse;
    }
  }

  @media screen and (min-width: ${devices.desktop.width - 1}px) {
    .navbar-header-actions {
      position: absolute;
      right: 120px;
    }
  }

  .navbar-header-actions {
    display: flex;
    flex-grow: 2;
    justify-content: flex-end;
    .navbar-item {
      padding: 0.65rem 0.75rem;
    }
  }

  .navbar-start .navbar-item {
    border-bottom: solid 5px transparent;
    &.is-active {
      border-bottom: solid 5px #28b1e8;
    }
  }

  .navbar-menu.is-active .navbar-start .navbar-item {
    border-bottom: none;
    border-left: solid 5px transparent;
    &.is-active {
      border-left: solid 5px #28b1e8;
    }
  }

  .navbar-menu {
    padding: 0;
  }

  .navbar-brand {
    @media screen and (max-width: ${devices.desktop.width - 1}px) {
      border-bottom: 1px solid var(--scm-white-color);
    }
  }

  .navbar-menu.is-active .navbar-end .navbar-item {
    border-left: solid 5px transparent;
  }
`;

type Props = {
  links: Links;
};

const NavigationBar: FC<Props> = ({ links }) => {
  const [burgerActive, setBurgerActive] = useState(false);
  const [t] = useTranslation("commons");
  const notificationsRef = useRef<HTMLButtonElement>(null);
  useEffect(() => {
    const close = (event: MouseEvent) => {
      const classList = event.target.classList.value;
      if (
        !classList.includes("navbar-burger") &&
        !classList.includes("navbar-brand") &&
        !classList.includes("search-box")
      ) {
        setBurgerActive(false);
      }
    };
    window.addEventListener("click", close);
    return () => window.removeEventListener("click", close);
  }, [burgerActive]);
  return (
    <StyledNavBar className="navbar is-fixed-top has-scm-background" aria-label="main navigation">
      <div className="container">
        <div className="navbar-brand">
          <LogoItem className="navbar-item logo">
            <Logo withText={false} className="image is-32x32" />
          </LogoItem>
          <button
            className={classNames("navbar-burger", { "is-active": burgerActive })}
            aria-haspopup="menu"
            aria-expanded={burgerActive}
            onClick={() => setBurgerActive((active) => !active)}
            aria-label={t("primary-navigation.navbarBurger")}
          >
            <span aria-hidden="true" />
            <span aria-hidden="true" />
            <span aria-hidden="true" />
          </button>
        </div>
        <StyledMenuBar className={classNames("navbar-menu", { "is-active": burgerActive })}>
          <div className="navbar-start">
            <PrimaryNavigation links={links} />
          </div>
          <div className="is-active navbar-header-actions">
            <Alerts className="navbar-item" />
            <OmniSearch links={links} shouldClear={true} ariaId="navbar" nextFocusRef={notificationsRef} />
            <Notifications ref={notificationsRef} className="navbar-item" />
          </div>
          <div className="navbar-end">
            <LogoutButton burgerMode={burgerActive} links={links} />
            <LoginButton burgerMode={burgerActive} links={links} />
          </div>
        </StyledMenuBar>
      </div>
    </StyledNavBar>
  );
};

export default NavigationBar;
