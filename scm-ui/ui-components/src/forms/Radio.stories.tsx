import React from "react";
import { storiesOf } from "@storybook/react";
import Radio from "./Radio";
import styled from "styled-components";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("Forms|Radio", module)
  .add("Default", () => (
    <Spacing>
      <Radio label="Not checked" checked={false} />
      <Radio label="Checked" checked={true} />
    </Spacing>
  ))
  .add("Disabled", () => (
    <Spacing>
      <Radio label="Checked but disabled" checked={true} disabled={true} />
    </Spacing>
  ));
