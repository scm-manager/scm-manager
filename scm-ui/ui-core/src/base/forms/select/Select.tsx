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

import React, { InputHTMLAttributes, Key, OptionHTMLAttributes } from "react";
import classNames from "classnames";
import { createVariantClass, Variant } from "../variants";
import { createAttributesForTesting } from "../../helpers";

type Props = {
  variant?: Variant;
  options?: Array<OptionHTMLAttributes<HTMLOptionElement> & { label: string }>;
  testId?: string;
} & InputHTMLAttributes<HTMLSelectElement>;

/**
 * @beta
 * @since 2.44.0
 */
const Select = React.forwardRef<HTMLSelectElement, Props>(
  ({ variant, children, className, options, testId, ...props }, ref) => (
    <div className={classNames("select", { "is-multiple": props.multiple }, createVariantClass(variant), className)}>
      <select ref={ref} {...props} {...createAttributesForTesting(testId)} className={className}>
        {options
          ? options.map((opt) => (
              <option {...opt} key={opt.value as Key}>
                {opt.label}
                {opt.children}
              </option>
            ))
          : children}
      </select>
    </div>
  )
);

export default Select;
