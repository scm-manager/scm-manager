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

import React, { useState } from "react";
import { storiesOf } from "@storybook/react";
import RadioButton from "./RadioButton";
import RadioGroup from "./RadioGroup";
import RadioGroupField from "./RadioGroupField";
import Form from "../Form";
import ControlledRadioGroupField from "./ControlledRadioGroupField";
import ControlledInputField from "../input/ControlledInputField";
import FormRow from "../FormRow";
import { ScmFormListContextProvider } from "../ScmFormListContext";
import ControlledList from "../list/ControlledList";
import ControlledTable from "../table/ControlledTable";
import ControlledColumn from "../table/ControlledColumn";
import AddListEntryForm from "../AddListEntryForm";

storiesOf("Radio Group", module)
  .add("Uncontrolled State", () => {
    return (
      <RadioGroup name="starter_pokemon">
        <RadioButton value="CHARMANDER" label="Charmander" />
        <RadioButton value="SQUIRTLE" label="Squirtle" />
        <RadioButton value="BULBASAUR" label="Bulbasaur" helpText="Dont pick this one" />
      </RadioGroup>
    );
  })
  .add("Controlled State", () => {
    const [value, setValue] = useState<string | undefined>(undefined);
    return (
      <RadioGroup name="starter_pokemon" value={value} onValueChange={setValue}>
        <RadioButton value="CHARMANDER" label="Charmander" />
        <RadioButton value="SQUIRTLE" label="Squirtle" />
        <RadioButton value="BULBASAUR" label="Bulbasaur" helpText="Dont pick this one" />
      </RadioGroup>
    );
  })
  .add("Radio Group Field", () => {
    const [value, setValue] = useState<string | undefined>(undefined);
    return (
      <RadioGroupField
        label="Choose your starter"
        helpText="Choose wisely"
        name="starter_pokemon"
        value={value}
        onValueChange={setValue}
      >
        <RadioButton value="CHARMANDER" label="Charmander" />
        <RadioButton value="SQUIRTLE" label="Squirtle" />
        <RadioButton value="BULBASAUR" label="Bulbasaur" helpText="Dont pick this one" />
      </RadioGroupField>
    );
  })
  .add("Controlled Radio Group Field", () => (
    <Form
      // eslint-disable-next-line no-console
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{ name: "", starter_pokemon: null }}
    >
      <FormRow>
        <ControlledInputField name="name" />
      </FormRow>
      <FormRow>
        <ControlledRadioGroupField
          name="starter_pokemon"
          label="Starter Pokemon"
          helpText="Choose wisely"
          rules={{ required: true }}
        >
          <RadioButton value="CHARMANDER" label="Charmander" labelClassName="is-block" />
          <RadioButton value="SQUIRTLE" label="Squirtle" />
          <RadioButton value="BULBASAUR" label="Bulbasaur" helpText="Dont pick this one" />
        </ControlledRadioGroupField>
      </FormRow>
    </Form>
  ))
  .add("Readonly Controlled Radio Group Field", () => (
    <Form
      // eslint-disable-next-line no-console
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{ name: "Red", starter_pokemon: "SQUIRTLE" }}
    >
      <FormRow>
        <ControlledInputField name="name" />
      </FormRow>
      <FormRow>
        <ControlledRadioGroupField
          name="starter_pokemon"
          label="Starter Pokemon"
          helpText="Choose wisely"
          readOnly={true}
        >
          <RadioButton value="CHARMANDER" label="Charmander" labelClassName="is-block" />
          <RadioButton value="SQUIRTLE" label="Squirtle" />
          <RadioButton value="BULBASAUR" label="Bulbasaur" helpText="Dont pick this one" />
        </ControlledRadioGroupField>
      </FormRow>
    </Form>
  ))
  .add("With options", () => (
    <Form
      // eslint-disable-next-line no-console
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{ starter_pokemon: "CHARMANDER" }}
    >
      <FormRow>
        <ControlledRadioGroupField
          name="starter_pokemon"
          label="Starter Pokemon"
          helpText="Choose wisely"
          rules={{ required: true }}
          options={[
            { value: "CHARMANDER", label: "Charmander" },
            { value: "SQUIRTLE", label: "Squirtle" },
            { value: "BULBASAUR", label: "Bulbasaur", helpText: "Dont pick this one" },
          ]}
        />
      </FormRow>
    </Form>
  ))
  .add("Without label prop", () => (
    <Form
      // eslint-disable-next-line no-console
      onSubmit={console.log}
      translationPath={["sample", "form"]}
      defaultValues={{ starter_pokemon: "CHARMANDER" }}
    >
      <FormRow>
        <ControlledRadioGroupField
          name="starter_pokemon"
          label="Starter Pokemon"
          helpText="Choose wisely"
          rules={{ required: true }}
          options={[
            { value: "CHARMANDER" },
            { value: "SQUIRTLE" },
            { value: "BULBASAUR", helpText: "Dont pick this one" },
          ]}
        />
      </FormRow>
    </Form>
  ))
  .add("Nested", () => (
    <Form
      translationPath={["sample", "form"]}
      // eslint-disable-next-line no-console
      onSubmit={console.log}
      defaultValues={{
        trainers: [
          {
            name: "Red",
            team: [{ nickname: "Charlie", pokemon: "CHARMANDER" }],
          },
          {
            name: "Blue",
            team: [{ nickname: "Squirtle", pokemon: "SQUIRTLE" }],
          },
          {
            name: "Green",
            team: [{ nickname: "Plant", pokemon: "SQUIRTLE" }],
          },
        ],
      }}
    >
      <ScmFormListContextProvider name={"trainers"}>
        <ControlledList withDelete>
          {({ value: trainers }) => (
            <>
              <FormRow>
                <ControlledInputField name={"name"} />
              </FormRow>
              <details className="has-background-dark-25 mb-2 p-2">
                <summary className="is-clickable">Team</summary>
                <div>
                  <ScmFormListContextProvider name={"team"}>
                    <ControlledTable withDelete>
                      <ControlledColumn name="nickname" />
                      <ControlledColumn name="pokemon" />
                    </ControlledTable>
                    <AddListEntryForm defaultValues={{ nickname: "", pokemon: null }}>
                      <FormRow>
                        <ControlledInputField name="nickname" />
                      </FormRow>
                      <FormRow>
                        <ControlledRadioGroupField
                          name="pokemon"
                          label="Starter Pokemon"
                          rules={{ required: true }}
                          options={[{ value: "CHARMANDER" }, { value: "SQUIRTLE" }, { value: "BULBASAUR" }]}
                        />
                      </FormRow>
                    </AddListEntryForm>
                  </ScmFormListContextProvider>
                </div>
              </details>
            </>
          )}
        </ControlledList>
      </ScmFormListContextProvider>
    </Form>
  ));
