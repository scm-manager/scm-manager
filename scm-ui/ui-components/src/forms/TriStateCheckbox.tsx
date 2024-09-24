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
import Icon from "../Icon";

type Props = {
  checked: boolean;
  indeterminate?: boolean;
  disabled?: boolean;
  label?: string;
  testId?: string;
};

/**
 * @deprecated
 */
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
    color = "secondary";
  } else if (checked || indeterminate) {
    color = "link";
  } else {
    color = "secondary-most";
  }

  // We need a tabIndex to make the checkbox accessible from keyboard.
  // We also add the gwt-Anchor css class to support the key-jump browser extension
  // https://github.com/KennethSundqvist/key-jump-chrome-extension/blob/master/src/content.js#L365
  return (
    <span tabIndex={0} className="gwt-Anchor">
      <Icon iconStyle={"is-outlined"} name={icon} className={className} color={color} testId={testId} /> {label}
    </span>
  );
};

export default TriStateCheckbox;
