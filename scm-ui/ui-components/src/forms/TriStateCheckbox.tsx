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
import Icon from "../Icon";

type Props = {
  checked: boolean;
  indeterminate?: boolean;
  disabled?: boolean;
  label?: string;
  testId?: string;
};

const TriStateCheckbox: FC<Props> = ({ checked, indeterminate, disabled, label, testId }) => {
  let icon;
  if (indeterminate) {
    icon = "minus-square";
  } else if (checked) {
    icon = "check-square";
  } else {
    icon = "square";
  }

  let className;
  if (!checked || indeterminate) {
    className = "far";
  } else {
    className = "fa";
  }

  let color;
  if (disabled) {
    color = "grey-light";
  } else if (checked || indeterminate) {
    color = "link";
  } else {
    color = "black";
  }

  return (
    <>
      <Icon iconStyle={"is-outlined"} name={icon} className={className} color={color} testId={testId} /> {label}
    </>
  );
};

export default TriStateCheckbox;
