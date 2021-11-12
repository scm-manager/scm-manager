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
import styled from "styled-components";
import Icon from "./Icon";

const Wrapper = styled.div`
  * {
    margin: 0.5rem;
  }
`;

const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];
const sizing = ["xs", "sm", "lg", "2x", "3x", "5x", "7x", "10x"];

storiesOf("Icon", module)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <>
      <Icon name="cat" />
      <Icon title="Download" name="download" color="info" />
      <Icon title="Pull Request" name="code-branch fa-rotate-180" color="warning" />
      <Icon title="Star" iconStyle="far" name="star" color="inherit" />
    </>
  ))
  .add("Colors", () => (
    <>
      <Icon title="default color" name="cat" />
      {colors.map((color) => (
        <Icon key={color} title={color} name="cat" color={color} />
      ))}
    </>
  ))
  .add("Sizing", () => (
    <>
      <Icon title="default size" name="cat" />
      {sizing.map((size) => (
        <Icon key={size} title={"fa-" + size} name={"cat fa-" + size} />
      ))}
    </>
  ))
  .add("Icon styles", () => (
    <>
      <Icon title="solid style" name="star" color="inherit" />
      <Icon title="regular style" iconStyle="far" name="star" color="inherit" />
      <Icon title="brand style" iconStyle="fab" name="react" color="inherit" />
    </>
  ))
  .add("More options", () => (
    <>
      <Icon title="rotate-270" name="snowboarding fa-rotate-270" />
      <Icon title="spin" name="spinner fa-spin" />
      <Icon title="custom sizing" name="cat" className="is-size-4" />
      <Icon title="custom background" name="hand-sparkles" className="has-background-primary-25" />
    </>
  ));
