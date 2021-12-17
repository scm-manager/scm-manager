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
  width: ${props => props.width}px;
  display: block;

  &:before {
    position: absolute;
    content: "";
    border-style: solid;
    pointer-events: none;
    height: 0;
    width: 0;
    top: 100%;
    left: ${props => props.width / 2}px;
    border-color: transparent;
    border-bottom-color: white;
    border-left-color: white;
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

const Popover: FC<Props> = props => {
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
      type: "enter-popover"
    });
  };

  const onMouseLeave = () => {
    dispatch({
      type: "leave-popover"
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
  width: 120
};

export default Popover;
