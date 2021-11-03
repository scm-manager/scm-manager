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
import { ReactNode, useState } from "react";
import { MemoryRouter } from "react-router-dom";
import { storiesOf } from "@storybook/react";
import Notification from "./Notification";

const Wrapper = styled.div`
  margin: 2rem;
  max-width: 400px;
`;

const content =
  "Cleverness nuclear genuine static irresponsibility invited President Zaphod\n" +
  "Beeblebrox hyperspace ship. Another custard through computer-generated universe\n" +
  "shapes field strong disaster parties Russell’s ancestors infinite colour\n" +
  "imaginative generator sweep.";

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Notification", module)
  .addDecorator(RoutingDecorator)
  .addDecorator((storyFn) => <Wrapper>{storyFn()}</Wrapper>)
  .add("Primary", () => <Notification type="primary">{content}</Notification>)
  .add("Success", () => <Notification type="success">{content}</Notification>)
  .add("Info", () => <Notification type="info">{content}</Notification>)
  .add("Warning", () => <Notification type="warning">{content}</Notification>)
  .add("Danger", () => <Notification type="danger">{content}</Notification>)
  .add("Closeable", () => <Closeable />);

const Closeable = () => {
  const [show, setShow] = useState(true);

  const hide = () => {
    setShow(false);
  };

  if (!show) {
    return null;
  }

  return (
    <Notification type="warning" onClose={() => hide()}>
      {content}
    </Notification>
  );
};
