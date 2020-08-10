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
import { storiesOf } from "@storybook/react";
import Radio from "./Radio";
import styled from "styled-components";

const Spacing = styled.div`
  padding: 2em;
`;

const RadioList = styled.div`
  display: flex;
  flex-direction: column;
  > label:not(:last-child) {
    margin-bottom: 0.75rem;
  }
  padding: 2em;
`;

storiesOf("Forms|Radio", module)
  .add("Default", () => (
    <Spacing>
      <Radio label="Not checked" checked={false} />
      <Radio label="Checked" checked={true} />
    </Spacing>
  ))
  .add("Disabled", () => (
    <Spacing>
      <Radio label="Checked but disabled" checked={true} disabled={true} />
    </Spacing>
  ))
  .add("With HelpText", () => (
    <RadioList>
      <Radio label="Classic helpText" checked={false} helpText="This is a classic help text." />
      <Radio
        label="Long helpText"
        checked={true}
        helpText="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
      />
    </RadioList>
  ));
