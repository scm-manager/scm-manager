import React from "react";
import Button from "./Button";
import { storiesOf } from "@storybook/react";
import StoryRouter from "storybook-react-router";
import styled from "styled-components";

const colors = ["primary", "success", "info", "warning", "danger", "black"];

const Spacing = styled.div`
  padding: 1em;
`;

storiesOf("Button", module)
  .addDecorator(StoryRouter())
  .add("Colors", () => (
    <div>
      {colors.map(color => (
        <Spacing>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ));
