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
import { storiesOf } from "@storybook/react";
import React, { ReactNode } from "react";
import Tag from "./Tag";
import { MemoryRouter } from "react-router-dom";
import { colors, sizes } from "./styleConstants";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const Spacing = styled.div`
  padding: 1em;
`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Tag", module)
  .addDecorator(RoutingDecorator)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => <Tag label="Default tag" />)
  .add("With Icon", () => <Tag label="System" icon="bolt" />)
  .add("Colors", () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Tag color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ))
  .add("With title", () => <Tag label="hover me" title="good job" />)
  .add("Clickable", () => <Tag label="Click here" onClick={() => alert("Not so fast")} />)
  .add("Sizes", () => (
    <div>
      {sizes.map((size) => (
        <Spacing key={size}>
          <Tag size={size} label={size} />
        </Spacing>
      ))}
    </div>
  ));
