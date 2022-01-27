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
import React, { FC, ReactNode, useEffect, useState } from "react";
import styled from "styled-components";
import classNames from "classnames";

// See for css reference: https://github.com/Wikiki/bulma-tooltip/blob/master/src/sass/index.sass

const TooltipWrapper = styled.span`
  position: relative;
  display: inline-block;
`;

const ArrowBase = styled.span`
  z-index: 1020;
  position: absolute;
  width: 0;
  height: 0;
  border-style: solid;
`;

const ArrowTop = styled(ArrowBase)`
  top: 0;
  right: auto;
  bottom: auto;
  left: 50%;
  margin-top: -5px;
  margin-right: auto;
  margin-bottom: auto;
  margin-left: -5px;
  border-width: 5px 5px 0 5px;
`;

const ArrowRight = styled(ArrowBase)`
  top: auto;
  right: 0;
  bottom: 50%;
  left: auto;
  margin-top: auto;
  margin-right: -5px;
  margin-bottom: -5px;
  margin-left: auto;
  border-width: 5px 5px 5px 0;
`;

const ArrowLeft = styled(ArrowBase)`
  top: auto;
  right: auto;
  bottom: 50%;
  left: 0;
  margin-top: auto;
  margin-right: auto;
  margin-bottom: -5px;
  margin-left: -5px;
  border-width: 5px 0 5px 5px;
`;

const ArrowBottom = styled(ArrowBase)`
  top: auto;
  right: auto;
  bottom: 0;
  left: 50%;
  margin-top: auto;
  margin-right: auto;
  margin-bottom: -5px;
  margin-left: -5px;
  border-width: 0 5px 5px 5px;
`;

const Arrow = {
  bottom: ArrowBottom,
  left: ArrowLeft,
  right: ArrowRight,
  top: ArrowTop
};

const TooltipContainerBase = styled.div<{ multiline?: boolean }>`
  z-index: 1020;
  position: absolute;
  padding: 0.5rem 1rem;
  overflow: hidden;
  hyphens: auto;
  text-overflow: ${({ multiline }) => (multiline ? "clip" : "ellipsis")};
  white-space: ${({ multiline }) => (multiline ? "normal" : "pre")};
  max-width: ${({ multiline }) => (multiline ? "15rem" : "auto")};
  width: ${({ multiline }) => (multiline ? "15rem" : "auto")};
  word-break: ${({ multiline }) => (multiline ? "keep-all" : "unset")};
`;

const TooltipContainerTop = styled(TooltipContainerBase)`
  left: 50%;
  bottom: calc(100% + 5px);
  transform: translateX(-50%);
`;

const TooltipContainerBottom = styled(TooltipContainerBase)`
  left: 50%;
  top: calc(100% + 5px);
  transform: translateX(-50%);
`;

const TooltipContainerLeft = styled(TooltipContainerBase)`
  right: calc(100% + 5px);
  top: 50%;
  transform: translateY(-50%);
`;

const TooltipContainerRight = styled(TooltipContainerBase)`
  left: calc(100% + 5px);
  top: 50%;
  transform: translateY(-50%);
`;

const Container = {
  bottom: TooltipContainerBottom,
  left: TooltipContainerLeft,
  right: TooltipContainerRight,
  top: TooltipContainerTop
};

type Props = {
  message: string;
  className?: string;
  location?: TooltipLocation;
  multiline?: boolean;
  children: ReactNode;
  id?: string;
};

export type TooltipLocation = "bottom" | "right" | "top" | "left";

const Tooltip: FC<Props> = ({ className, message, location = "right", multiline, children, id }) => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const listener = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false);
      }
    };
    window.addEventListener("keydown", listener);
    return () => window.removeEventListener("keydown", listener);
  }, []);

  const LocationContainer = Container[location];
  const LocationArrow = Arrow[location];

  return (
    <TooltipWrapper
      onMouseEnter={() => setOpen(true)}
      onMouseLeave={() => setOpen(false)}
      onClick={() => setOpen(false)}
    >
      {open ? (
        <>
          <LocationArrow className={`tooltip-arrow-${location}-border-color`} />
          <LocationContainer
            className={classNames(
              className,
              "is-size-7",
              "is-family-primary",
              "has-rounded-border",
              "has-text-white",
              "has-background-grey-dark",
              "has-text-weight-semibold"
            )}
            multiline={multiline}
            aria-live="polite"
            id={id}
            role="tooltip"
          >
            {message}
          </LocationContainer>
        </>
      ) : null}
      {children}
    </TooltipWrapper>
  );
};

export default Tooltip;
