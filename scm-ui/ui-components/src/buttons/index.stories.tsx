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

import React, { ReactElement, ReactNode, useEffect, useState } from "react";
import Button from "./Button";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import AddButton from "./AddButton";
import CreateButton from "./CreateButton";
import DeleteButton from "./DeleteButton";
import DownloadButton from "./DownloadButton";
import EditButton from "./EditButton";
import SubmitButton from "./SubmitButton";
import { MemoryRouter } from "react-router-dom";

const colors = ["primary", "link", "info", "success", "warning", "danger", "white", "light", "dark", "black", "text"];

const Spacing = styled.div`
  padding: 1em;
`;

const SpacingDecorator = (story: () => ReactNode) => <Spacing>{story()}</Spacing>;
const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Buttons/Button", module)
  .addDecorator(RoutingDecorator)
  .add("Colors", () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ))
  .add("Loading", () => (
    <Spacing>
      <Button color={"primary"} loading={true}>
        Loading Button
      </Button>
    </Spacing>
  ))
  .add("Disabled", () => (
    <div>
      {colors.map((color) => (
        <Spacing key={color}>
          <Button color={color} label={color} disabled={true} />
        </Spacing>
      ))}
    </div>
  ));

const buttonStory = (name: string, storyFn: () => ReactElement) => {
  return storiesOf("Buttons/" + name, module)
    .addDecorator(RoutingDecorator)
    .addDecorator(SpacingDecorator)
    .add("Default", storyFn);
};
buttonStory("AddButton", () => <AddButton>Add</AddButton>);
buttonStory("CreateButton", () => <CreateButton>Create</CreateButton>);
buttonStory("DeleteButton", () => <DeleteButton>Delete</DeleteButton>);
buttonStory("DownloadButton", () => <DownloadButton displayName="Download" disabled={false} url="" />).add(
  "Disabled",
  () => <DownloadButton displayName="Download" disabled={true} url="" />
);
buttonStory("EditButton", () => <EditButton>Edit</EditButton>);
buttonStory("SubmitButton", () => <SubmitButton>Submit</SubmitButton>);
buttonStory("Button Ref", () => {
  const [ref, setRef] = useState<HTMLButtonElement | HTMLAnchorElement | null>();
  useEffect(() => ref?.focus(), [ref]);
  return <Button ref={setRef}>Focus me!</Button>;
});
