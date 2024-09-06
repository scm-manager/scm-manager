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
import { storiesOf } from "@storybook/react";
import DateFromNow from "./DateFromNow";
import DateShort from "./DateShort";

const baseProps = {
  timeZone: "Europe/Berlin",
  baseDate: "2019-10-12T13:56:42+02:00",
};

const dates = [
  "2009-06-30T18:30:00+02:00",
  "2019-06-30T18:30:00+02:00",
  "2019-10-12T13:56:40+02:00",
  "2019-10-11T13:56:40+02:00",
];

storiesOf("Date", module)
  .add("Date from now", () => (
    <div className="p-5">
      {dates.map((d) => (
        <p>
          <DateFromNow date={d} {...baseProps} />
        </p>
      ))}
    </div>
  ))
  .add("Short", () => (
    <div className="p-5">
      {dates.map((d) => (
        <p>
          <DateShort date={d} {...baseProps} />
        </p>
      ))}
    </div>
  ));
