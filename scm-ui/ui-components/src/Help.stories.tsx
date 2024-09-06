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

import * as React from "react";
import { storiesOf } from "@storybook/react";
import Help from "./Help";

const longContent =
  "Cleverness nuclear genuine static irresponsibility invited President Zaphod\n" +
  "Beeblebrox hyperspace ship. Another custard through computer-generated universe\n" +
  "shapes field strong disaster parties Russell’s ancestors infinite colour\n" +
  "imaginative generator sweep.";

storiesOf("Help", module)
  .addDecorator((storyFn) => <div className="m-6">{storyFn()}</div>)
  .add("Default", () => <Help message="This is a help message" />)
  .add("Multiline", () => (
    <>
      <div className="mt-4">
        <label>With multiline (default):</label>
        <Help message={longContent} />
      </div>
      <div className="mt-4">
        <label>Without multiline:</label>
        <Help message={longContent} multiline={false} />
      </div>
    </>
  ));
