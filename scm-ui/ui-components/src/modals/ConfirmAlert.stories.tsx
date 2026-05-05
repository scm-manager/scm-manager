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

// import { storiesOf } from "@storybook/react";
// import { MemoryRouter } from "react-router-dom";
// import * as React from "react";
// import ConfirmAlert, { confirmAlert } from "./ConfirmAlert";
// import ActiveModalCountContext from "./activeModalCountContext";
//
// const body =
//   "Mind-paralyzing change needed improbability vortex machine sorts sought same theory upending job just allows\n " +
//   "hostess’s really oblong Infinite Improbability thing into the starship against which behavior accordance with\n " +
//   "Kakrafoon humanoid undergarment ship powered by GPP-guided bowl of petunias nothing was frequently away incredibly\n " +
//   "ordinary mob.";
//
// const buttons = [
//   {
//     className: "is-outlined",
//     label: "Cancel",
//     onClick: () => null,
//   },
//   {
//     label: "Submit",
//   },
// ];
//
// const buttonsWithAutofocus = [
//   {
//     label: "Cancel",
//     onClick: () => null,
//   },
//   {
//     className: "is-info",
//     label: "I should be focused",
//     autofocus: true,
//   },
// ];
//
// const doNothing = () => {
//   // Do nothing
// };
//
// storiesOf("Modal/ConfirmAlert", module)
//   .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
//   .addDecorator((story) => (
//     <ActiveModalCountContext.Provider value={{ value: 0, increment: doNothing, decrement: doNothing }}>
//       {story()}
//     </ActiveModalCountContext.Provider>
//   ))
//   .add("Default", () => <ConfirmAlert message={body} title="Are you sure about that?" buttons={buttons} />)
//   .add("WithButton", () => {
//     const buttonClick = () => {
//       confirmAlert({ message: body, title: "Are you sure about that?", buttons });
//     };
//     return (
//       <>
//         <button onClick={buttonClick}>Open ConfirmAlert</button>
//         <div id="modalRoot" />
//       </>
//     );
//   })
//   .add("Autofocus", () => (
//     <ConfirmAlert message={body} title="Are you sure about that?" buttons={buttonsWithAutofocus} />
//   ));

import React, { useState } from "react";
import type { Meta, StoryObj } from "@storybook/react";
import ConfirmAlert from "./ConfirmAlert";
import ActiveModalCountContext from "./activeModalCountContext";

// --- Helfer-Funktionen und Mock-Daten (aus der Original-Story übernommen) ---

const body =
  "Mind-paralyzing change needed improbability vortex machine sorts sought same theory upending job just allows\n " +
  "hostess’s really oblong Infinite Improbability thing into the starship against which behavior accordance with\n " +
  "Kakrafoon humanoid undergarment ship powered by GPP-guided bowl of petunias nothing was frequently away incredibly\n " +
  "ordinary mob.";

const buttons = [{ className: "is-outlined", label: "Cancel", onClick: () => null }, { label: "Submit" }];

const buttonsWithAutofocus = [
  { label: "Cancel", onClick: () => null },
  { className: "is-info", label: "I should be focused", autofocus: true },
];

const doNothing = () => {
  // Do nothing
};

// Helfer-Komponente für die Story, die den Alert per Klick auslöst
const TriggerConfirmAlertExample = () => {
  const [show, setShow] = useState(false);
  const handleClick = () => {
    setShow((old) => !old);
  };
  return (
    <>
      <p>
        Klicken Sie auf den Button, um den Bestätigungsdialog imperativ auszulösen. Der Dialog wird in{" "}
        <code>#modalRoot</code> gerendert.
      </p>
      <button className="button" onClick={handleClick}>
        Open ConfirmAlert
      </button>
      {show && <ConfirmAlert title="Are you sure about that?" buttons={buttons} />}
      {/* Das ist notwendig, damit die `confirmAlert`-Funktion ein Einhängepunkt im DOM findet. */}
    </>
  );
};

// --- Storybook Metadaten ---

const meta: Meta<typeof ConfirmAlert> = {
  title: "Modal/ConfirmAlert",
  component: ConfirmAlert,
  // Der ActiveModalCountContext.Provider wird als Decorator für alle Stories beibehalten.
  decorators: [
    (Story) => (
      <ActiveModalCountContext.Provider value={{ value: 0, increment: doNothing, decrement: doNothing }}>
        <div style={{ margin: "2rem" }}>
          <Story />
        </div>
      </ActiveModalCountContext.Provider>
    ),
  ],
  tags: ["autodocs"],
};

export default meta;

// --- Story-Definitionen ---

type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    message: body,
    title: "Are you sure about that?",
    buttons: buttons,
  },
};

export const Autofocus: Story = {
  args: {
    ...Default.args,
    buttons: buttonsWithAutofocus,
  },
};

// Diese Story verwendet eine `render`-Funktion, um die imperative API zu demonstrieren.
export const TriggeredByButton: Story = {
  name: "Triggered by Button",
  render: () => <TriggerConfirmAlertExample />,
};
