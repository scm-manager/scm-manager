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
import { MemoryRouter } from "react-router-dom";
import { storiesOf } from "@storybook/react";
import CardColumnSmall from "./CardColumnSmall";
import Icon from "./Icon";
import styled from "styled-components";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const link = "/foo/bar";
const avatar = <Icon name="icons fa-2x fa-fw" alt="avatar" />;
const contentLeft = <strong className="m-0">main content</strong>;
const contentRight = <small>more text</small>;

storiesOf("CardColumnSmall", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <CardColumnSmall link={link} avatar={avatar} contentLeft={contentLeft} contentRight={contentRight} />
  ))
  .add("Minimal", () => <CardColumnSmall link={link} contentLeft={contentLeft} contentRight={contentRight} />)
  .add("Task", () => (
    <CardColumnSmall
      link={link}
      avatar={<Icon name="exchange-alt" className="fa-fw fa-lg" color="inherit" alt="avatar" />}
      contentLeft={<strong>Repository created</strong>}
      contentRight={<small>over 42 years ago</small>}
      footer="New: scmadmin/spaceship"
    />
  ))
  .add("Linkless", () => (
    <CardColumnSmall
      avatar={<Icon name="eraser" className="fa-fw fa-lg" color="inherit" alt="avatar" />}
      contentLeft={<strong>Repository deleted</strong>}
      contentRight={<small>over 1337 minutes ago</small>}
      footer="Deleted: scmadmin/spaceship"
    />
  ));
