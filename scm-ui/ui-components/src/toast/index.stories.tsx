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
import React, { useState } from "react";
import { storiesOf } from "@storybook/react";
import Toast from "./Toast";
import ToastButtons from "./ToastButtons";
import ToastButton from "./ToastButton";
import { types } from "./themes";
import ToastArea from "./ToastArea";
import ToastNotification from "./ToastNotification";

const toastStories = storiesOf("Toast", module);

const AnimatedToast = () => (
  <Toast type="primary" title="Animated">
    Awesome animated Toast
  </Toast>
);

const Animator = () => {
  const [display, setDisplay] = useState(false);

  return (
    <div style={{ padding: "2rem" }}>
      {display && <AnimatedToast />}
      <button className="button is-primary" onClick={() => setDisplay(!display)}>
        {display ? "Close" : "Open"} Toast
      </button>
    </div>
  );
};

const Closeable = () => {
  const [show, setShow] = useState(true);

  const hide = () => {
    setShow(false);
  };

  if (!show) {
    return null;
  }

  return (
    <Toast type="success" title="Awesome feature">
      <p>Close the message with a click</p>
      <ToastButtons>
        <ToastButton icon="times" onClick={hide}>
          Click to close
        </ToastButton>
      </ToastButtons>
    </Toast>
  );
};

toastStories.add("Open/Close", () => <Animator />);
toastStories.add("Click to close", () => <Closeable />);

types.forEach((type) => {
  toastStories.add(type.charAt(0).toUpperCase() + type.slice(1), () => (
    <Toast type={type} title="New Changes">
      <p>The underlying Pull-Request has changed. Press reload to see the changes.</p>
      <p>Warning: Non saved modification will be lost.</p>
      <ToastButtons>
        <ToastButton icon="redo">Reload</ToastButton>
        <ToastButton icon="times">Ignore</ToastButton>
      </ToastButtons>
    </Toast>
  ));
});

toastStories.add("Multiple", () => (
  <ToastArea>
    {types.map((type) => (
      <ToastNotification key={type} type={type} title="New notification">
        <p>The notification received.</p>
      </ToastNotification>
    ))}
  </ToastArea>
));
