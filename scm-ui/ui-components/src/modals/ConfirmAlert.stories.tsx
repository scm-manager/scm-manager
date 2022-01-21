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
import ConfirmAlert, { confirmAlert } from "./ConfirmAlert";

const body =
  "Mind-paralyzing change needed improbability vortex machine sorts sought same theory upending job just allows\n " +
  "hostess’s really oblong Infinite Improbability thing into the starship against which behavior accordance with\n " +
  "Kakrafoon humanoid undergarment ship powered by GPP-guided bowl of petunias nothing was frequently away incredibly\n " +
  "ordinary mob.";

const buttons = [
  {
    className: "is-outlined",
    label: "Cancel",
    onClick: () => null
  },
  {
    label: "Submit"
  }
];

const buttonsWithAutofocus = [
  {
    className: "is-outlined",
    label: "Cancel",
    onClick: () => null
  },
  {
    label: "I should be focused",
    autofocus: true
  }
];

storiesOf("Modal/ConfirmAlert", module)
  .addDecorator(story => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .add("Default", () => <ConfirmAlert message={body} title={"Are you sure about that?"} buttons={buttons} />)
  .add("WithButton", () => {
    const buttonClick = () => {
      confirmAlert({ message: body, title: "Are you sure about that?", buttons });
    };
    return (
      <>
        <button onClick={buttonClick}>Open ConfirmAlert</button>
        <div id="modalRoot" />
      </>
    );
  })
  .add("Autofocus", () => (
    <ConfirmAlert message={body} title={"Are you sure about that?"} buttons={buttonsWithAutofocus} />
  ));
