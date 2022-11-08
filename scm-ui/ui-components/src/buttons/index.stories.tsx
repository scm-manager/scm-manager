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
