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
import React, { FC } from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import Icon from "./Icon";

const Wrapper = styled.div`
  * {
    margin: 0.5rem;
  }
`;

const Container: FC = ({ children }) => <Wrapper>{children}</Wrapper>;

const helloWorld = () => {
  alert("Hello world!");
};

storiesOf("Icon", module)
  .addDecorator(storyFn => <Container>{storyFn()}</Container>)
  .add("default", () => (
    <>
      <Icon title="Download" name="download" color="info" />
      <Icon title="Pull Request" name="code-branch fa-rotate-180" color="warning" />
      <Icon title="Star" iconStyle="far" name="star" color="inherit" />
      <Icon
        title="Hello world!"
        name="hand-sparkles"
        className="has-background-primary-25"
        onClick={() => helloWorld()}
      />
    </>
  ))
  .add("minimal", () => (
    <>
      <Icon name="download" />
      <Icon name="icons" />
      <Icon name="cat" />
    </>
  ));
