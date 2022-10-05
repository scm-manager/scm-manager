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
import React, { FC } from "react";
import Logo from "./../Logo";
import styled from "styled-components";
import { devices } from "../devices";

type Props = {
  authenticated?: boolean;
};

const StyledSmallHeader = styled.nav`
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

const SmallHeader: FC = ({ children }) => {
  return (
    <StyledSmallHeader className="navbar is-fixed-top has-scm-background" aria-label="main navigation">
      <div className="container">{children}</div>
    </StyledSmallHeader>
  );
};

const LargeHeader: FC = () => {
  return (
    <div className="hero has-scm-background is-small">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-vcentered">
            <div className="column">
              <Logo />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const Header: FC<Props> = ({ authenticated, children }) => {
  if (authenticated) {
    return <SmallHeader>{children}</SmallHeader>;
  } else {
    return <LargeHeader />;
  }
};

export default Header;
