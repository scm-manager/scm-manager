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
import Icon from "../Icon";
import { storiesOf } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import React from "react";
import GroupEntry from "./GroupEntry";
import { RepositoryFlag } from "../repos";
import { Button, ButtonGroup } from "../buttons";
import copyToClipboard from "../CopyToClipboard";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 200px;
`;

const link = "/foo/bar";
const icon = <Icon name="icons fa-2x fa-fw" />;
const name = <strong className="is-marginless">main content</strong>;
const description = <small>more text</small>;
const contentRight = (
  <ButtonGroup>
    <Button
      icon={"download"}
      title={"Copy clone command to clipboard"}
      action={() => copyToClipboard("git clone {url}")}
    />
  </ButtonGroup>
);

storiesOf("GroupEntry", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <GroupEntry
      link={link}
      avatar={icon}
      name={name}
      description={description}
      contentRight={contentRight}
    />
  ));
