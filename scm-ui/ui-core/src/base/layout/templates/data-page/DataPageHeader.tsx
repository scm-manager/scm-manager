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

import withClasses from "../../_helpers/with-classes";
import React, { HTMLAttributes } from "react";
import classNames from "classnames";
import { useAriaId } from "../../../helpers";
import { LinkButton } from "../../../buttons";

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeader = withClasses("div", [
  "is-flex",
  "is-flex-wrap-wrap",
  "is-justify-content-space-between",
  "mb-3",
  "has-row-gap-2",
  "has-column-gap-4",
]);

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeaderSettings = withClasses("div", [
  "is-flex",
  "is-flex-wrap-wrap",
  "is-align-items-center",
  "has-row-gap-2",
  "has-column-gap-4",
  "is-flex-grow-1",
  "is-flex-shrink-1",
  "is-flex-basis-0",
]);

type DataPageHeaderSettingProps = HTMLAttributes<HTMLDivElement> & {
  children?: React.ReactNode | ((props: { formFieldId: string }) => React.ReactNode);
};

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeaderSetting = React.forwardRef<HTMLSpanElement, DataPageHeaderSettingProps>(
  ({ className, children, ...props }, ref) => {
    const formFieldId = useAriaId();
    return (
      <span {...props} className={classNames(className, "is-flex", "is-align-items-center", "has-gap-2")} ref={ref}>
        {typeof children === "function" ? children({ formFieldId }) : children}
      </span>
    );
  }
);

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeaderSettingLabel = withClasses("label", [
  "is-flex",
  "is-align-items-center",
  "is-text-wrap-no-wrap",
]);

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeaderSettingField = React.Fragment;

/**
 * @beta
 * @since 2.47.0
 */
export const DataPageHeaderCreateButton = withClasses(LinkButton, ["is-flex-grow-0", "is-flex-shrink-0"], {
  variant: "primary",
});
