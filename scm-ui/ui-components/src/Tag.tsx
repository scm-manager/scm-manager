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
  children,
}) => {
  const [t] = useTranslation("commons");

  let showIcon = null;
  if (icon) {
    showIcon = (
      <>
        <i className={classNames("fas", `fa-${icon}`)} aria-hidden="true" />
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
      {onClick === undefined ? (
        <span
          className={classNames(
            "tag",
            `is-${color}`,
            `is-${size}`,
            className,
            {
              "is-outlined": outlined,
              "is-rounded": rounded,
            },
            size === "small" && smallClassNames
          )}
          title={title}
        >
          {showIcon}
          {label}
          {children}
        </span>
      ) : (
        <button
          className={classNames(
            "tag",
            `is-${color}`,
            `is-${size}`,
            className,
            {
              "is-outlined": outlined,
              "is-rounded": rounded,
            },
            size === "small" && smallClassNames
          )}
          onClick={onClick}
        >
          {showIcon}
          {label}
          {children}
        </button>
      )}
      {showDelete}
    </>
  );
};

export default Tag;
