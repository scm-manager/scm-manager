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

import { storiesOf } from "@storybook/react";
import Duration from "./Duration";
import React from "react";

storiesOf("Duration", module).add("Duration", () => (
  <div className="m-5 p-5">
    <p>
      <Duration duration={1} />
    </p>
    <p>
      <Duration duration={500} />
    </p>
    <p>
      <Duration duration={1000 + 1} />
    </p>
    <p>
      <Duration duration={2000} />
    </p>
    <p>
      <Duration duration={1000 * 60 + 1} />
    </p>
    <p>
      <Duration duration={42 * 1000 * 60} />
    </p>
    <p>
      <Duration duration={1000 * 60 * 60 + 1} />
    </p>
    <p>
      <Duration duration={21 * 1000 * 60 * 60} />
    </p>
    <p>
      <Duration duration={1000 * 60 * 60 * 24 + 1} />
    </p>
    <p>
      <Duration duration={5 * 1000 * 60 * 60 * 24} />
    </p>
    <p>
      <Duration duration={1000 * 60 * 60 * 24 * 7 + 1} />
    </p>
    <p>
      <Duration duration={3 * 1000 * 60 * 60 * 24 * 7} />
    </p>
    <p>
      <Duration duration={12 * 1000 * 60 * 60 * 24} />
    </p>
  </div>
));
