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

import Icon from "../Icon";
import { storiesOf } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import React from "react";
import GroupEntry from "./GroupEntry";
import { Button, ButtonGroup } from "../buttons";
import copyToClipboard from "../CopyToClipboard";

const link = "/foo/bar";
const icon = <Icon name="icons fa-2x fa-fw" />;
const name = <strong className="m-0">main content</strong>;
const description = <small>more text</small>;
const longName = (
  <strong className="m-0">
    Very-important-repository-with-a-particular-long-but-easily-rememberable-name-which-also-is-written-in-kebab-case
  </strong>
);
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
  .addDecorator((storyFn) => <div className="m-5">{storyFn()}</div>)
  .add("Default", () => (
    <GroupEntry link={link} avatar={icon} name={name} description={description} contentRight={contentRight} />
  ))
  .add("With long texts", () => (
    <GroupEntry
      link={link}
      avatar={icon}
      name={longName}
      description={
        <small>
          Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et
          dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet
          clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet,
          consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,
          sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea
          takimata sanctus est Lorem ipsum dolor sit amet.
        </small>
      }
      contentRight={contentRight}
    />
  ));
