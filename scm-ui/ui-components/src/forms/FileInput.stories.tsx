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

import React, { FC, useState } from "react";
import { MemoryRouter } from "react-router-dom";
import { useForm } from "react-hook-form";
import { storiesOf } from "@storybook/react";
import { Level, SubmitButton } from "../index";
import FileInput from "./FileInput";
import styled from "styled-components";

const Decorator = styled.div`
  padding: 2rem;
  max-width: 40rem;
`;

type Settings = {
  uploadFile: string;
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
        <FileInput label="Upload File" helpText="Select your most loved file" {...register("uploadFile")} />
        <Level right={<SubmitButton>Submit</SubmitButton>} />
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

storiesOf("Forms/FileInput", module)
  .addDecorator((storyFn) => <Decorator>{storyFn()}</Decorator>)
  .addDecorator((storyFn) => <MemoryRouter>{storyFn()}</MemoryRouter>)
  .add("Default", () => <ReactHookForm />);
