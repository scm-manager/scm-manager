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
import type { Meta, StoryObj } from "@storybook/react";

import DateFromNow from "./DateFromNow";

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

const meta: Meta<typeof DateFromNow> = {
  title: "Components/DateFromNow",
  component: DateFromNow,
  decorators: [(Story) => <div className="p-5">{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof meta>;

export const Primary: Story = {
  args: {
    date: dates[3],
    ...baseProps,
  },
};

export const AllExamples: Story = {
  render: () => (
    <div>
      {dates.map((d) => (
        <p key={d.toString()}>
          <DateFromNow date={d} {...baseProps} />
        </p>
      ))}
    </div>
  ),
};
