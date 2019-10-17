import React from "react";
import Button from "./Button";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import { MemoryRouter } from 'react-router-dom';

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
  .addDecorator(story => (
    <MemoryRouter initialEntries={['/']}>{story()}</MemoryRouter>
  ))
  .add("Colors", () => (
    <div>
      {colors.map(color => (
        <Spacing key={color}>
          <Button color={color} label={color} />
        </Spacing>
      ))}
    </div>
  ));
