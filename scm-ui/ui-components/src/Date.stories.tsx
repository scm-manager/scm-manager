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
import React from "react";
import DateFromNow from "./DateFromNow";
import { storiesOf } from "@storybook/react";
import DateShort from "./DateShort";
import styled from "styled-components";

const baseProps = {
  timeZone: "Europe/Berlin",
  baseDate: "2019-10-12T13:56:42+02:00"
};

const dates = [
  "2009-06-30T18:30:00+02:00",
  "2019-06-30T18:30:00+02:00",
  "2019-10-12T13:56:40+02:00",
  "2019-10-11T13:56:40+02:00"
];

const Wrapper = styled.div`
  padding: 2rem;
`;

storiesOf("Date", module)
  .add("Date from now", () => (
    <Wrapper>
      {dates.map(d => (
        <p>
          <DateFromNow date={d} {...baseProps} />
        </p>
      ))}
    </Wrapper>
  ))
  .add("Short", () => (
    <Wrapper>
      {dates.map(d => (
        <p>
          <DateShort date={d} {...baseProps} />
        </p>
      ))}
    </Wrapper>
  ));
