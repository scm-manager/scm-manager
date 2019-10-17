// @flow
import React, { type Node } from "react";
import Button from "./Button";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import { MemoryRouter } from "react-router-dom";
import AddButton from "./AddButton";
import CreateButton from "./CreateButton";
import DeleteButton from "./DeleteButton";
import DownloadButton from "./DownloadButton";
import EditButton from "./EditButton";
import SubmitButton from "./SubmitButton";

const colors = [
  "primary",
  "link",
  "info",
  "success",
  "warning",
  "danger",
  "white",
  "light",
  "dark",
  "black",
  "text"
];

const Spacing = styled.div`
  padding: 1em;
`;

const RoutingDecorator = story => (
  <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>
);

const SpacingDecorator = story => <Spacing>{story()}</Spacing>;

storiesOf("Buttons|Button", module)
  .addDecorator(RoutingDecorator)
  .add("Colors", () => (
    <div>
      {colors.map(color => (
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
  ));

const buttonStory = (name: string, storyFn: () => Node) => {
  return storiesOf("Buttons|" + name, module)
    .addDecorator(RoutingDecorator)
    .addDecorator(SpacingDecorator)
    .add("Default", storyFn);
};

buttonStory("AddButton", () => <AddButton color={"primary"}>Add</AddButton>);
buttonStory("CreateButton", () => <CreateButton>Create</CreateButton>);
buttonStory("DeleteButton", () => <DeleteButton>Delete</DeleteButton>);
buttonStory("DownloadButton", () => (
  <DownloadButton
    displayName="Download"
    disabled={false}
    url=""
  ></DownloadButton>
));
buttonStory("EditButton", () => <EditButton>Edit</EditButton>);
buttonStory("SubmitButton", () => <SubmitButton>Submit</SubmitButton>);
