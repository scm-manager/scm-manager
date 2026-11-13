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

import React, { FC, HTMLAttributes, Ref } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";

type Props = HTMLAttributes<HTMLSpanElement> & {
  /**
   * Text announced by screen readers, defaults to a generic "Loading ..."
   */
  label?: string;
  ref?: Ref<HTMLSpanElement>;
};

/**
 * Small loading indicator for places where {@link Loading} is too large,
 * for example inside of table cells, tags or next to a text.
 *
 * Inside a {@link Button}, use its `isLoading` property instead.
 */
const SmallLoadingSpinner: FC<Props> = ({ label, className, ref, ...props }) => {
  const [t] = useTranslation("commons");
  return (
    <span role="status" className={classNames("loader-wrapper", className)} {...props} ref={ref}>
      <span className="loader is-loading" aria-hidden="true" />
      <span className="is-sr-only">{label ?? t("loading.alt")}</span>
    </span>
  );
};

export default SmallLoadingSpinner;
