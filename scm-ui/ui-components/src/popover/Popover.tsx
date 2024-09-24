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

import React, { Dispatch, FC, ReactNode, useLayoutEffect, useRef, useState } from "react";
import { Action } from "./usePopover";
import styled from "styled-components";

type Props = {
  title: ReactNode;
  width?: number;
  // props should be defined by usePopover
  offsetTop?: number;
  offsetLeft?: number;
  show: boolean;
  dispatch: Dispatch<Action>;
};

type ContainerProps = {
  width: number;
};

const PopoverContainer = styled.div<ContainerProps>`
  position: absolute;
  z-index: 100;
  width: ${(props) => props.width}px;
  display: block;

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 100%;
    left: ${(props) => props.width / 2}px;
    border-color: transparent;
    border-bottom-color: var(--scm-popover-border-color);
    border-left-color: var(--scm-popover-border-color);
    border-width: 0.4rem;
    margin-left: -0.4rem;
    margin-top: -0.4rem;
    -webkit-transform-origin: center;
    transform-origin: center;
    transform: rotate(-45deg);
  }
`;

const PopoverHeading = styled.div`
  height: 1.5em;
`;

/**
 * @deprecated use {@link ui-overlays/popover} instead
 */
const Popover: FC<Props> = (props) => {
  if (!props.show) {
    return null;
  }
  return <InnerPopover {...props} />;
};

const InnerPopover: FC<Props> = ({ title, show, width, offsetTop, offsetLeft, dispatch, children }) => {
  const [height, setHeight] = useState(125);
  const ref = useRef<HTMLDivElement>(null);
  useLayoutEffect(() => {
    if (ref.current) {
      setHeight(ref.current.clientHeight);
    }
  }, [ref]);

  const onMouseEnter = () => {
    dispatch({
      type: "enter-popover",
    });
  };

  const onMouseLeave = () => {
    dispatch({
      type: "leave-popover",
    });
  };

  const top = (offsetTop || 0) - height - 5;
  const left = (offsetLeft || 0) - width! / 2;
  return (
    <PopoverContainer
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      className="box popover"
      style={{ top: `${top}px`, left: `${left}px` }}
      width={width!}
      ref={ref}
    >
      <PopoverHeading>{title}</PopoverHeading>
      <hr className="my-2" />
      {children}
    </PopoverContainer>
  );
};

Popover.defaultProps = {
  width: 120,
};

export default Popover;
