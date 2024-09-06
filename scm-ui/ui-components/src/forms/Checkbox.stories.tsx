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

import React, { FC, useRef, useState } from "react";
import { storiesOf } from "@storybook/react";
import Checkbox from "./Checkbox";
import styled from "styled-components";
import Button from "../buttons/Button";
import { useForm } from "react-hook-form";
import { SubmitButton } from "../buttons";
import { MemoryRouter } from "react-router-dom";

const Spacing = styled.div`
  padding: 2em;
`;

const Ref: FC = () => {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <>
      <Checkbox label={"Ref Checkbox"} checked={false} ref={ref} />
      <Button
        action={() => {
          if (ref.current) {
            ref.current.checked = !ref.current.checked;
          }
        }}
        color="primary"
      >
        Toggle Checkbox
      </Button>
    </>
  );
};

type Settings = {
  rememberMe: string;
  scramblePassword: string;
  readonly: boolean;
  disabled: boolean;
};

const ReactHookForm: FC = () => {
  const { register, handleSubmit } = useForm<Settings>({
    defaultValues: {
      disabled: true,
      readonly: true,
    },
  });
  const [stored, setStored] = useState<Settings>();

  const onSubmit = (settings: Settings) => {
    setStored(settings);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
        <Checkbox label="Remember Me" {...register("rememberMe")} />
        <Checkbox label="Scramble Password" {...register("scramblePassword")} />
        <Checkbox label="Disabled wont be submitted" disabled={true} {...register("disabled")} />
        <Checkbox label="Readonly will be submitted" readOnly={true} {...register("readonly")} />
        <div className="pt-2">
          <SubmitButton>Submit</SubmitButton>
        </div>
      </form>
      {stored ? (
        <div className="mt-5">
          <pre>
            <code>{JSON.stringify(stored, null, 2)}</code>
          </pre>
        </div>
      ) : null}
    </>
  );
};

const LegacyEvents: FC = () => {
  const [value, setValue] = useState<boolean>(false);
  return (
    <>
      <Checkbox checked={value} onChange={setValue} />
      <div className="mt-3">{JSON.stringify(value)}</div>
    </>
  );
};

storiesOf("Forms/Checkbox", module)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("Default", () => (
    <Spacing>
      <Checkbox label="Not checked" checked={false} />
      <Checkbox label="Checked" checked={true} />
      <Checkbox label="Indeterminate" checked={true} indeterminate={true} />
    </Spacing>
  ))
  .add("Disabled", () => (
    <Spacing>
      <Checkbox label="Checked but disabled" checked={true} disabled={true} />
    </Spacing>
  ))
  .add("With HelpText", () => (
    <Spacing>
      <Checkbox label="Classic helpText" checked={false} helpText="This is a classic help text." />
      <Checkbox
        label="Long helpText"
        checked={true}
        helpText="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
      />
    </Spacing>
  ))
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("ReactHookForm", () => <ReactHookForm />);
