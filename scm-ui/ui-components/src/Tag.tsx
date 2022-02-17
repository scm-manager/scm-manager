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
import { Color, Size } from "./styleConstants";
import { useTranslation } from "react-i18next";

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

const smallClassNames = classNames("p-1", "is-size-7", "has-text-weight-bold");

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
  children
}) => {
  const [t] = useTranslation("commons");

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
    showDelete = <button className="tag is-delete" onClick={onRemove} aria-label={t("tag.delete")} />;
  }

  return (
    <>
      <span
        className={classNames(
          "tag",
          `is-${color}`,
          `is-${size}`,
          className,
          {
            "is-outlined": outlined,
            "is-rounded": rounded,
            "is-clickable": onClick
          },
          size === "small" && smallClassNames
        )}
        title={title}
        onClick={onClick}
      >
        {showIcon}
        {label}
        {children}
      </span>
      {showDelete}
    </>
  );
};

export default Tag;
