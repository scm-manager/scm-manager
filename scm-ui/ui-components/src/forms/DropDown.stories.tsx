/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
