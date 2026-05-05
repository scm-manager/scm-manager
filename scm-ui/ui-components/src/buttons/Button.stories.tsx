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

// import React, {ReactElement, ReactNode, useEffect, useState} from "react";
// import styled from "styled-components";
// import {Meta, storiesOf, StoryObj} from "@storybook/react";
// import Button from "./Button";
// import {MemoryRouter} from "react-router-dom";
// import AddButton from "./AddButton";
// import CreateButton from "./CreateButton";
// import DeleteButton from "./DeleteButton";
// import DownloadButton from "./DownloadButton";
// import EditButton from "./EditButton";
// import SubmitButton from "./SubmitButton";

// const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];
//
// const Spacing = styled.div`
//   padding: 1em;
// `;
//
// const SpacingDecorator = (story: () => ReactNode) => <Spacing>{story()}</Spacing>;
// const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;
//
// storiesOf("Buttons/Button", module)
//   .addDecorator(RoutingDecorator)
//   .add("Colors", () => (
//     <div>
//       {colors.map((color) => (
//         <Spacing key={color}>
//           <Button color={color} label={color} />
//         </Spacing>
//       ))}
//     </div>
//   ))
//   .add("Loading", () => (
//     <Spacing>
//       <Button color="primary" loading={true}>
//         Loading Button
//       </Button>
//     </Spacing>
//   ))
//   .add("Disabled", () => (
//     <div>
//       {colors.map((color) => (
//         <Spacing key={color}>
//           <Button color={color} label={color} disabled={true} />
//         </Spacing>
//       ))}
//     </div>
//   ));
//
// const buttonStory = (name: string, storyFn: () => ReactElement) => {
//   return storiesOf("Buttons/" + name, module)
//     .addDecorator(RoutingDecorator)
//     .addDecorator(SpacingDecorator)
//     .add("Default", storyFn);
// };
// buttonStory("AddButton", () => <AddButton>Add</AddButton>);
// buttonStory("CreateButton", () => <CreateButton>Create</CreateButton>);
// buttonStory("DeleteButton", () => <DeleteButton>Delete</DeleteButton>);
// buttonStory("DownloadButton", () => <DownloadButton displayName="Download" disabled={false} url="" />).add(
//   "Disabled",
//   () => <DownloadButton displayName="Download" disabled={true} url="" />
// );
// buttonStory("EditButton", () => <EditButton>Edit</EditButton>);
// buttonStory("SubmitButton", () => <SubmitButton>Submit</SubmitButton>);
// buttonStory("Button Ref", () => {
//   const [ref, setRef] = useState<HTMLButtonElement | HTMLAnchorElement | null>();
//   useEffect(() => ref?.focus(), [ref]);
//   return <Button ref={setRef}>Focus me!</Button>;
// });

import React, { useEffect, useState } from "react";
import styled from "styled-components";
import type { Meta, StoryObj } from "@storybook/react";
import Button from "./Button";

// --- Helfer-Komponenten und Mock-Daten ---

const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];
const Spacing = styled.div`
  padding: 1em;
  display: inline-block;
`;

const RefExample = () => {
  const [ref, setRef] = useState<HTMLButtonElement | HTMLAnchorElement | null>(null);
  useEffect(() => {
    ref?.focus();
  }, [ref]);
  return <Button ref={setRef}>Focus me!</Button>;
};

// --- Storybook Metadaten ---

const meta: Meta<typeof Button> = {
  title: "Buttons/Button",
  component: Button,
  decorators: [(Story) => <Spacing>{Story()}</Spacing>],
  tags: ["autodocs"],
};

export default meta;

// --- Story-Definitionen ---

// @ts-ignore This type is too complex, no clue what to do about it
type Story = StoryObj<typeof meta>;

export const Loading: Story = {
  args: {
    color: "primary",
    loading: true,
    children: "Loading Button",
  },
};

// Stories mit komplexem Layout verwenden eine `render`-Funktion
export const Colors: Story = {
  render: () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ),
};

export const Disabled: Story = {
  render: () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} disabled={true} />
        </Spacing>
      ))}
    </div>
  ),
};

export const UsingRefs: Story = {
  name: "Using Refs",
  render: () => <RefExample />,
};
