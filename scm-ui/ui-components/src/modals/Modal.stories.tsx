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

import { storiesOf } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import * as React from "react";
import { useState } from "react";
import Modal from "./Modal";

const body = (
  <p>
    Mind-paralyzing change needed improbability vortex machine sorts sought same theory upending job just allows
    hostess’s really oblong Infinite Improbability thing into the starship against which behavior accordance.with
    Kakrafoon humanoid undergarment ship powered by GPP-guided bowl of petunias nothing was frequently away incredibly
    ordinary mob.
  </p>
);

storiesOf("Modal|Modal", module)
  .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .add("Default", () => <CloseableModal />);

const CloseableModal = () => {
  const [show, setShow] = useState(true);

  const toggleModal = () => {
    setShow(!show);
  };

  return <Modal body={body} closeFunction={toggleModal} active={show} title={"Hitchhiker Modal"} />;
};
