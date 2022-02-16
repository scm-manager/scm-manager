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

import styled from "styled-components";
import * as React from "react";
import { ReactNode } from "react";
import { MemoryRouter } from "react-router-dom";
import { storiesOf } from "@storybook/react";
import Tooltip, { TooltipLocation } from "./Tooltip";
import Button from "./buttons/Button";

const Wrapper = styled.div`
  margin: 2rem;
  width: 100%;
`;

const Spacing = styled.div`
  padding: 2em 6em;
  width: 50%;
  margin: 0 auto;
`;

const positions: TooltipLocation[] = ["right", "top", "left", "bottom"];

const message = "Heart of Gold";

const mutltiline = `Characters:

- Arthur Dent
- Ford Prefect
- Zaphod Beeblebrox
- Marvin the Paranoid Android
- Trillian
- Slartibartfast`;

const shortMultiline = `* a
* b
* c`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Tooltip", module)
  .addDecorator(RoutingDecorator)
  .add("Default", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={message} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Multiline", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={mutltiline} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Short Multiline", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={shortMultiline} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ))
  .add("Styled", () => (
    <Wrapper>
      {positions.map((position) => (
        <Spacing>
          <Tooltip message={message} location={position} className={"has-text-warning"}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </Wrapper>
  ));
