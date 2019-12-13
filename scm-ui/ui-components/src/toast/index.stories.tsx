import React, { useState } from "react";
import { storiesOf } from "@storybook/react";
import Toast from "./Toast";
import ToastButtons from "./ToastButtons";
import ToastButton from "./ToastButton";
import { types } from "./themes";

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

toastStories.add("Open/Close", () => <Animator />);

types.forEach(type => {
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
