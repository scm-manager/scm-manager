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
import classNames from "classnames";
import { createAttributesForTesting } from "./devBuild";

type Props = {
  title?: string;
  iconStyle?: string;
  name: string;
  color?: string;
  className?: string;
  onClick?: (event: React.MouseEvent) => void;
  onEnter?: (event: React.KeyboardEvent) => void;
  testId?: string;
  tabIndex?: number;
  alt?: string;
};

/**
 * @deprecated
 */
const Icon: FC<Props> = ({
  iconStyle = "fas",
  color = "secondary",
  title,
  name,
  className,
  onClick,
  testId,
  tabIndex,
  onEnter,
  alt = title,
}) => {
  return (
    <i
      onClick={onClick}
      onKeyPress={(event) => event.key === "Enter" && onEnter && onEnter(event)}
      title={title}
      className={classNames(iconStyle, "fa-fw", "fa-" + name, `has-text-${color}`, className)}
      tabIndex={tabIndex}
      aria-label={alt}
      {...createAttributesForTesting(testId)}
    />
  );
};

export default Icon;
