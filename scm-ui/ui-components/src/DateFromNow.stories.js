import React from "react";
import DateFromNow from "./DateFromNow";
import { storiesOf } from "@storybook/react";

storiesOf("DateFromNow", module).add("Default", () => (
  <div>
    <p>
      <DateFromNow date="2009-06-30T18:30:00+02:00" />
    </p>
    <p>
      <DateFromNow date="2019-06-30T18:30:00+02:00" />
    </p>
  </div>
));
