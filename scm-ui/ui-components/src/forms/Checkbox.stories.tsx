import React from "react";
import { storiesOf } from "@storybook/react";
import Checkbox from "./Checkbox";
import styled from "styled-components";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("Forms|Checkbox", module)
  .add("Default", () => (
    <Spacing>
      <Checkbox label="Not checked" checked={false} />
      <Checkbox label="Checked" checked={true} />
    </Spacing>
  ))
  .add("Disabled", () => (
    <Spacing>
      <Checkbox label="Checked but disabled" checked={true} disabled={true} />
    </Spacing>
  ));
