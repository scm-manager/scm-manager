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
import React, { FC, HTMLAttributes } from "react";
import classNames from "classnames";
import { Color, Size } from "./styleConstants";
import styled, { css } from "styled-components";

type Props = {
  className?: string;
  color?: Color;
  outlined?: boolean;
  rounded?: boolean;
  icon?: string;
  label?: string;
  title?: string;
  size?: Size;
  onClick?: () => void;
  onRemove?: () => void;
};

type InnerTagProps = HTMLAttributes<HTMLSpanElement> & {
  small: boolean;
};

const smallMixin = css`
  font-size: 0.7rem !important;
  padding: 0.25rem !important;
  font-weight: bold;
`;

const InnerTag = styled.span<InnerTagProps>`
  ${(props) => props.small && smallMixin};
`;

const Tag: FC<Props> = ({
  className,
  color = "light",
  outlined,
  size = "normal",
  rounded,
  icon,
  label,
  title,
  onClick,
  onRemove,
  children,
}) => {
  let showIcon = null;
  if (icon) {
    showIcon = (
      <>
        <i className={classNames("fas", `fa-${icon}`)} />
        &nbsp;
      </>
    );
  }
  let showDelete = null;
  if (onRemove) {
    showDelete = <a className="tag is-delete" onClick={onRemove} />;
  }

  return (
    <>
      <InnerTag
        className={classNames("tag", `is-${color}`, `is-${size}`, className, {
          "is-outlined": outlined,
          "is-rounded": rounded,
          "has-cursor-pointer": onClick,
        })}
        title={title}
        onClick={onClick}
        small={size === "small"}
      >
        {showIcon}
        {label}
        {children}
      </InnerTag>
      {showDelete}
    </>
  );
};

export default Tag;
