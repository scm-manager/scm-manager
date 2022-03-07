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
import styled, { css } from "styled-components";
import { devices, ErrorNotification, Loading } from "@scm-manager/ui-components";
import classNames from "classnames";

type DropDownMenuProps = {
  mobilePosition: "left" | "right";
};

const DropDownMenu = styled.div<DropDownMenuProps>`
  min-width: 20rem;

  @media screen and (min-width: ${devices.desktop.width}px) {
    min-width: 30rem;
  }

  @media screen and (min-width: ${devices.tablet.width}px) {
    min-width: 25rem;
  }

  @media screen and (max-width: ${devices.mobile.width}px) {
    ${props =>
      props.mobilePosition === "right" &&
      css`
        right: -1.5rem;
        left: auto;
      `};
    position: fixed;
    top: auto;
  }

  @media screen and (max-width: ${devices.desktop.width - 1}px) {
    margin-right: 1rem;
  }

  @media screen and (min-width: ${devices.desktop.width}px) {
    right: 0;
    left: auto;
  }

  &:before {
    position: absolute;
    content: "";
    pointer-events: none;
    height: 0;
    width: 0;
    top: -7px; // top padding of dropdown-menu + border-spacing
    transform-origin: center;
    transform: rotate(135deg);
    
    @media screen and (max-width: ${devices.mobile.width}px)  {
        left: 4.6rem;
      }

    @media screen and (min-width: ${devices.mobile.width + 1}px) and (max-width: ${devices.desktop.width-1}px)  {
      left: 1.3rem;
    }

    @media screen and (min-width: ${devices.desktop.width}px) {
      right: 1.375rem;
    }

    ${props =>
      props.mobilePosition === "right" &&
      css`
        @media screen and (max-width: ${devices.mobile.width}px) {
          left: auto;
          right: 1.75rem;
        }
      `};
  }
`;

export const Table = styled.table`
  border-collapse: collapse;
`;

export const Column = styled.td`
  vertical-align: middle !important;
`;

export const OnlyMobileWrappingColumn = styled(Column)`
  white-space: nowrap;
  @media screen and (max-width: ${devices.mobile.width}px) {
    white-space: break-spaces;
  }
`;

const DropdownMenuContainer: FC = ({ children }) => (
  <div className={classNames("dropdown-content", "p-4")}>{children}</div>
);

const ErrorBox: FC<{ error?: Error | null }> = ({ error }) => {
  if (!error) {
    return null;
  }
  return (
    <DropdownMenuContainer>
      <ErrorNotification error={error} />
    </DropdownMenuContainer>
  );
};

const LoadingBox: FC = () => (
  <div className="box">
    <Loading />
  </div>
);

const IconContainer = styled.div`
  width: 2rem;
  height: 2rem;
`;

type CounterProps = {
  count: string;
};

const Counter = styled.span<CounterProps>`
  position: absolute;
  top: -0.75rem;
  right: ${props => (props.count.length <= 1 ? "-0.25" : "-0.50")}rem;
`;

type IconWrapperProps = {
  icon: React.ReactNode;
  count?: string;
};

const IconWrapper: FC<IconWrapperProps> = ({ icon, count }) => (
  <IconContainer className={classNames("is-relative", "is-flex", "is-justify-content-center", "is-align-items-center")}>
    {icon}
    {count ? <Counter count={count}>{count}</Counter> : null}
  </IconContainer>
);

type Props = DropDownMenuProps & {
  className?: string;
  icon: React.ReactNode;
  count?: string;
  error?: Error | null;
  isLoading?: boolean;
};

const DropDownTrigger = styled.div`
  padding: 0.65rem 0.75rem;
`;

const HeaderDropDown: FC<Props> = ({ className, icon, count, error, isLoading, mobilePosition, children }) => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const close = () => setOpen(false);
    window.addEventListener("click", close);
    return () => window.removeEventListener("click", close);
  }, []);

  return (
    <>
      <div
        className={classNames(
          "notifications",
          "dropdown",
          "is-hoverable",
          "p-0",
          {
            "is-active": open
          },
          className
        )}
        onClick={e => e.stopPropagation()}
      >
        <DropDownTrigger
          className={classNames("is-flex", "dropdown-trigger", "is-clickable")}
          onClick={() => setOpen(o => !o)}
        >
          <IconWrapper icon={icon} count={count} />
        </DropDownTrigger>
        <DropDownMenu mobilePosition={mobilePosition} className="dropdown-menu pt-0" id="dropdown-menu" role="menu">
          <ErrorBox error={error} />
          {isLoading ? <LoadingBox /> : null}
          {children}
        </DropDownMenu>
      </div>
    </>
  );
};

export default HeaderDropDown;
