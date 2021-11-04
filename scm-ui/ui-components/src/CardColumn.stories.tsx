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
import CardColumn from "./CardColumn";
import Icon from "./Icon";
import styled from "styled-components";
import { DateFromNow } from ".";
import repository from "./__resources__/repository";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const link = "/foo/bar";
const avatar = <Icon name="icons fa-2x fa-fw" alt="avatar" />;
const title = <strong>title</strong>;
const footerLeft = <small>left footer</small>;
const footerRight = <small>right footer</small>;
const baseDate = "2020-03-26T12:13:42+02:00";

storiesOf("CardColumn", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Default", () => (
    <CardColumn
      link={link}
      avatar={avatar}
      title={title}
      description="A description can be added here."
      footerLeft={footerLeft}
      footerRight={footerRight}
    />
  ))
  .add("Minimal", () => <CardColumn title={title} footerLeft={footerLeft} footerRight={footerRight} />)
  .add("With hoverable date", () => (
    <CardColumn
      title={title}
      footerLeft={footerLeft}
      footerRight={
        <small className="level-item">
          <DateFromNow baseDate={baseDate} date={repository.creationDate} />
        </small>
      }
    />
  ));
