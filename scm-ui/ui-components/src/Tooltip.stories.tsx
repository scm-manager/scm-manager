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
import Tooltip from "./Tooltip";
import Button from "./buttons/Button";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const Spacing = styled.div`
  padding: 2em 6em;
`;

const positions = ["right", "top", "left", "bottom"];

const message = "Heart of Gold";

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Tooltip", module)
  .addDecorator(RoutingDecorator)
  .addDecorator(storyFn => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <div>
      {positions.map(position => (
        <Spacing>
          <Tooltip message={message} location={position}>
            <Button label={position} color="info" />{" "}
          </Tooltip>
        </Spacing>
      ))}
    </div>
  ));
