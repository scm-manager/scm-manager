/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FC, useEffect, useState } from "react";
import { Links } from "@scm-manager/ui-types";
import classNames from "classnames";
import styled from "styled-components";
import { devices, Logo, PrimaryNavigation } from "@scm-manager/ui-components";
import HeaderActions from "./HeaderActions";
import Notifications from "./Notifications";

const StyledMenuBar = styled.div`
  background-color: transparent !important;
`;

const LogoItem = styled.a`
  cursor: default !important;
`;

const StyledNavBar = styled.nav`
  @media screen and (min-width: ${devices.desktop.width - 1}px) {
    .navbar-burger-actions {
      display: none;
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
      border-bottom: 1px solid white;
    }
  }

  .navbar-menu.is-active .navbar-end .navbar-item {
    border-left: solid 5px transparent;
  }

  .navbar-burger {
    color: #fff !important;
  }

  .navbar-item {
    :hover:not(.logo) {
      background-color: rgba(10, 10, 10, 0.1) !important;
      color: #fff;
    }
    color: #fff !important;
    background-color: transparent !important;
  }
  color: #fff;
  background-color: transparent !important;
`;

type Props = {
  links: Links;
};

const BurgerActionBar: FC = () => (
  <div className="navbar-burger-actions">
    <Notifications className="navbar-item" direction="left" />
  </div>
);

const NavigationBar: FC<Props> = ({ links }) => {
  const [burgerActive, setBurgerActive] = useState(false);
  useEffect(() => {
    const close = () => {
      if (burgerActive) {
        setBurgerActive(false);
      }
    };
    window.addEventListener("click", close);
    return () => window.removeEventListener("click", close);
  }, [burgerActive]);

  return (
    <StyledNavBar className="navbar mb-0 container" role="navigation" aria-label="main navigation">
      <div className="navbar-brand">
        <LogoItem className="navbar-item logo">
          <Logo withText={false} className="image is-32x32" />
        </LogoItem>
        <BurgerActionBar />
        <button
          role="button"
          className={classNames("navbar-burger", { "is-active": burgerActive })}
          aria-expanded="true"
          onClick={() => setBurgerActive((active) => !active)}
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
        <div className="navbar-end">
          <HeaderActions burgerMode={burgerActive} links={links} />
        </div>
      </StyledMenuBar>
    </StyledNavBar>
  );
};

export default NavigationBar;
