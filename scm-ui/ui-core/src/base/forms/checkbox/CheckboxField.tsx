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

import React from "react";
import Field from "../base/Field";
import Control from "../base/Control";
import Checkbox from "./Checkbox";

type Props = React.ComponentProps<typeof Checkbox>;

const CheckboxField = React.forwardRef<HTMLInputElement, Props>(({ className, ...props }, ref) => (
  <Field className={className}>
    <Control>
      <Checkbox ref={ref} {...props} />
    </Control>
  </Field>
));
export default CheckboxField;
