/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
