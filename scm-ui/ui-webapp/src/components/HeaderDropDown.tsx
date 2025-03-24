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
    right: 0;
    left: auto;
    min-width: 30rem;
  }

  @media screen and (min-width: ${devices.tablet.width}px) {
    min-width: 25rem;
  }

  @media screen and (max-width: ${devices.mobile.width}px) {
    position: fixed;
    top: auto;
    width: 100%;
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

    @media screen and (max-width: ${devices.mobile.width}px) {
      left: 4.6rem;
    }

    @media screen and (min-width: ${devices.mobile.width + 1}px) and (max-width: ${devices.desktop.width - 1}px) {
      margin-right: 1rem;
      left: 1.3rem;
    }

    @media screen and (min-width: ${devices.desktop.width}px) {
      right: 1.375rem;
    }

    ${(props) =>
      props.mobilePosition === "right" &&
      css`
        @media screen and (max-width: ${devices.mobile.width}px) {
          left: 21.75rem;
        }

        @media screen and (min-width: ${devices.mobile.width + 1}px) and (max-width: ${devices.tablet.width - 1}px) {
          left: 19.65rem;
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
  right: ${(props) => (props.count.length <= 1 ? "-0.25" : "-0.50")}rem;
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

type Props = React.PropsWithChildren<
  DropDownMenuProps & {
    className?: string;
    icon: React.ReactNode;
    count?: string;
    error?: Error | null;
    isLoading?: boolean;
  }
>;

const DropDownTrigger = styled.div`
  padding: 0.65rem 0.75rem;
`;

const HeaderDropDown = React.forwardRef<HTMLButtonElement, Props>(
  ({ className, icon, count, error, isLoading, mobilePosition, children }, ref) => {
    const [open, setOpen] = useState(false);

    useEffect(() => {
      const close = () => setOpen(false);
      window.addEventListener("click", close);
      return () => window.removeEventListener("click", close);
    }, []);

    return (
      <button
        type="button"
        className={classNames(
          "notifications",
          "dropdown",
          "is-hoverable",
          "p-0",
          "is-borderless",
          "has-background-transparent",
          {
            "is-active": open,
          },
          className
        )}
        onClick={(e) => e.stopPropagation()}
        tabIndex={0}
        ref={ref}
      >
        <DropDownTrigger
          className={classNames("is-flex", "dropdown-trigger", "is-clickable")}
          onClick={() => setOpen((o) => !o)}
        >
          <IconWrapper icon={icon} count={count} />
        </DropDownTrigger>
        <DropDownMenu mobilePosition={mobilePosition} className="dropdown-menu pt-0" id="dropdown-menu" role="menu">
          <ErrorBox error={error} />
          {isLoading ? <LoadingBox /> : null}
          {children}
        </DropDownMenu>
      </button>
    );
  }
);

export default HeaderDropDown;
