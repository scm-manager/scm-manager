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
import { storiesOf } from "@storybook/react";
import Help from "./Help";

const Wrapper = styled.div`
  margin: 5rem;
`;

const Spacing = styled.div`
  margin-top: 1rem;
`;

const longContent =
  "Cleverness nuclear genuine static irresponsibility invited President Zaphod\n" +
  "Beeblebrox hyperspace ship. Another custard through computer-generated universe\n" +
  "shapes field strong disaster parties Russell’s ancestors infinite colour\n" +
  "imaginative generator sweep.";

storiesOf("Help", module)
  .addDecorator(storyFn => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => <Help message="This is a help message" />)
  .add("Multiline", () => (
    <>
      <Spacing>
        <label>With multiline (default):</label>
        <Help message={longContent} />
      </Spacing>
      <Spacing>
        <label>Without multiline:</label>
        <Help message={longContent} multiline={false} />
      </Spacing>
    </>
  ));
