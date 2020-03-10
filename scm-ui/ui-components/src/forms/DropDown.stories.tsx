import React from "react";
import { storiesOf } from "@storybook/react";
import DropDown from "./DropDown";

storiesOf("Forms|DropDown", module)
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
        "The Meaning Of Liff"
      ]}
      preselectedOption={"dirk"}
      optionSelected={selection => {
        // nothing to do
      }}
    />
  ));
