import React from "react";
import Button from "./Button";
import { storiesOf } from "@storybook/react";
import StoryRouter from "storybook-react-router";
import styled from "styled-components";

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

storiesOf("Button", module)
  .addDecorator(StoryRouter())
  .add("Colors", () => (
    <div>
      {colors.map(color => (
        <Spacing key={color}>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ));
