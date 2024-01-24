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
