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
import Radio from "./Radio";
import styled from "styled-components";
import Button from "../buttons/Button";
import { useForm } from "react-hook-form";
import { SubmitButton } from "../buttons";
import { MemoryRouter } from "react-router-dom";

const Spacing = styled.div`
  padding: 2em;
`;

const RadioList = styled.div`
  display: flex;
  flex-direction: column;
  > label:not(:last-child) {
    margin-bottom: 0.75rem;
  }
  padding: 2em;
`;

const Ref: FC = () => {
  const ref = useRef<HTMLInputElement>(null);
  return (
    <>
      <Radio label={"Ref Radio Button"} checked={false} ref={ref} />
      <Button
        action={() => {
          if (ref.current) {
            ref.current.checked = true;
          }
        }}
        color="primary"
      >
        Check InputField
      </Button>
    </>
  );
};

type Settings = {
  rememberMe: string;
  scramblePassword: string;
  readonly: string;
  disabled: string;
};

const ReactHookForm: FC = () => {
  const { register, handleSubmit } = useForm<Settings>();
  const [stored, setStored] = useState<Settings>();

  const onSubmit = (settings: Settings) => {
    setStored(settings);
  };

  return (
    <>
      <form onSubmit={handleSubmit(onSubmit)}>
        <RadioList>
          <Radio defaultChecked={true} value={"true"} label="Remember Me" {...register("rememberMe")} />
          <Radio value={"false"} label="Dont Remember Me" {...register("rememberMe")} />
        </RadioList>
        <Radio className="ml-2" value={"false"} label="Scramble Password" {...register("scramblePassword")} />
        <Radio value={"false"} label="Disabled wont be submitted" disabled={true} {...register("disabled")} />
        <Radio value={"false"} label="Readonly will be submitted" {...register("readonly")} />
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
      <Radio checked={value} onChange={setValue} />
      <div className="mt-3">{JSON.stringify(value)}</div>
    </>
  );
};

storiesOf("Forms/Radio", module)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
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
  ))
  .add("With HelpText", () => (
    <RadioList>
      <Radio label="Classic helpText" checked={false} helpText="This is a classic help text." />
      <Radio
        label="Long helpText"
        checked={true}
        helpText="Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."
      />
    </RadioList>
  ))
  .add("Ref", () => <Ref />)
  .add("Legacy Events", () => <LegacyEvents />)
  .add("ReactHookForm", () => <ReactHookForm />);
