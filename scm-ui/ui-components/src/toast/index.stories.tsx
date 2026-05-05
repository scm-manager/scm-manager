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

// import React, { useState } from "react";
// import { storiesOf } from "@storybook/react";
// import Toast from "./Toast";
// import ToastButtons from "./ToastButtons";
// import ToastButton from "./ToastButton";
// import { types } from "./themes";
// import ToastArea from "./ToastArea";
// import ToastNotification from "./ToastNotification";
//
// const toastStories = storiesOf("Toast", module);
//
// const AnimatedToast = () => (
//   <Toast type="primary" title="Animated">
//     Awesome animated Toast
//   </Toast>
// );
//
// const Animator = () => {
//   const [display, setDisplay] = useState(false);
//
//   return (
//     <div style={{ padding: "2rem" }}>
//       {display && <AnimatedToast />}
//       <button className="button is-primary" onClick={() => setDisplay(!display)}>
//         {display ? "Close" : "Open"} Toast
//       </button>
//     </div>
//   );
// };
//
// const Closeable = () => {
//   const [show, setShow] = useState(true);
//
//   const hide = () => {
//     setShow(false);
//   };
//
//   if (!show) {
//     return null;
//   }
//
//   return (
//     <Toast type="success" title="Awesome feature">
//       <p>Close the message with a click</p>
//       <ToastButtons>
//         <ToastButton icon="times" onClick={hide}>
//           Click to close
//         </ToastButton>
//       </ToastButtons>
//     </Toast>
//   );
// };
//
// toastStories.add("Open/Close", () => <Animator />);
// toastStories.add("Click to close", () => <Closeable />);
//
// types.forEach((type) => {
//   toastStories.add(type.charAt(0).toUpperCase() + type.slice(1), () => (
//     <Toast type={type} title="New Changes">
//       <p>The underlying Pull-Request has changed. Press reload to see the changes.</p>
//       <p>Warning: Non saved modification will be lost.</p>
//       <ToastButtons>
//         <ToastButton icon="redo">Reload</ToastButton>
//         <ToastButton icon="times">Ignore</ToastButton>
//       </ToastButtons>
//     </Toast>
//   ));
// });
//
// toastStories.add("Multiple", () => (
//   <ToastArea>
//     {types.map((type) => (
//       <ToastNotification key={type} type={type} title="New notification">
//         <p>The notification received.</p>
//       </ToastNotification>
//     ))}
//   </ToastArea>
// ));

import React, { useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import Toast from "./Toast";
import ToastButtons from "./ToastButtons";
import ToastButton from "./ToastButton";
import { types } from "./themes";
import ToastArea from "./ToastArea";
import ToastNotification from "./ToastNotification";

// --- Helfer-Komponenten (aus der Original-Story übernommen) ---

const OpenCloseExample = () => {
  const [display, setDisplay] = useState(false);
  return (
    <div>
      {display && (
        <Toast type="primary" title="Animated">
          Awesome animated Toast
        </Toast>
      )}
      <button className="button is-primary mt-2" onClick={() => setDisplay(!display)}>
        {display ? "Close" : "Open"} Toast
      </button>
    </div>
  );
};

const ClickToCloseExample = () => {
  const [show, setShow] = useState(true);
  if (!show) {
    return <p>Toast wurde geschlossen.</p>;
  }
  return (
    <Toast type="success" title="Awesome feature">
      <p>Close the message with a click</p>
      <ToastButtons>
        <ToastButton icon="times" onClick={() => setShow(false)}>
          Click to close
        </ToastButton>
      </ToastButtons>
    </Toast>
  );
};

// --- Storybook Metadaten ---

const meta: Meta<typeof Toast> = {
  title: "Components/Toast",
  component: Toast,
  decorators: [(Story) => <div style={{ padding: "2rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

// --- Story-Definitionen ---

type Story = StoryObj<typeof meta>;

// Basis-Props für die verschiedenen Toast-Typen
const baseArgs = {
  title: "New Changes",
  children: (
    <>
      <p>The underlying Pull-Request has changed. Press reload to see the changes.</p>
      <p>Warning: Non saved modification will be lost.</p>
      <ToastButtons>
        <ToastButton icon="redo">Reload</ToastButton>
        <ToastButton icon="times">Ignore</ToastButton>
      </ToastButtons>
    </>
  ),
};

// Anstatt einer Schleife erstellen wir explizite Stories für jeden Typ
export const Primary: Story = { args: { ...baseArgs, type: "primary" } };
export const Success: Story = { args: { ...baseArgs, type: "success" } };
export const Info: Story = { args: { ...baseArgs, type: "info" } };
export const Warning: Story = { args: { ...baseArgs, type: "warning" } };
export const Danger: Story = { args: { ...baseArgs, type: "danger" } };

// Stories mit eigener Logik verwenden eine `render`-Funktion
export const OpenClose: Story = {
  name: "Open and Close Animation",
  render: () => <OpenCloseExample />,
};

export const ClickToClose: Story = {
  name: "Click to Close",
  render: () => <ClickToCloseExample />,
};

export const Multiple: Story = {
  name: "Multiple Toasts in an Area",
  render: () => (
    <ToastArea>
      {types.map((type) => (
        <ToastNotification key={type} type={type} title="New notification">
          <p>The notification received.</p>
        </ToastNotification>
      ))}
    </ToastArea>
  ),
};
