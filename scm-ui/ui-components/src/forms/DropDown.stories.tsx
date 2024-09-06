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

import React from "react";
import { storiesOf } from "@storybook/react";
import DropDown from "./DropDown";

storiesOf("Forms/DropDown", module)
  .add("Default", () => (
    <DropDown
      options={["en", "de", "es"]}
      preselectedOption={"de"}
      optionSelected={() => {
        // nothing to do
      }}
    />
  ))
  .add("With Translation", () => (
    <DropDown
      optionValues={["hg2g", "dirk", "liff"]}
      options={[
        "The Hitchhiker's Guide to the Galaxy",
        "Dirk Gently’s Holistic Detective Agency",
        "The Meaning Of Liff",
      ]}
      preselectedOption={"dirk"}
      optionSelected={(selection) => {
        // nothing to do
      }}
    />
  ))
  .add("Add preselect if missing in options", () => (
    <DropDown
      optionValues={["alpha", "beta", "gamma"]}
      options={["A", "B", "C"]}
      preselectedOption={"D"}
      optionSelected={(selection) => {
        // nothing to do
      }}
    />
  ));
