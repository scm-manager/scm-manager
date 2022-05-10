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
import React, { FC, FormEvent, useState } from "react";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useConfigLink } from "@scm-manager/ui-api";
import ConfigurationForm from "./ConfigurationForm";

type RenderProps = {
  readOnly: boolean;
  initialConfiguration: HalRepresentation;
  onConfigurationChange: (configuration: HalRepresentation, valid: boolean) => void;
};

type Props = {
  link: string;
  render: (props: RenderProps) => JSX.Element;
};

const Configuration: FC<Props> = ({ link, render }) => {
  const { initialConfiguration, update, ...formProps } = useConfigLink<HalRepresentation>(link);
  const [configuration, setConfiguration] = useState<HalRepresentation>();
  const [isValid, setIsValid] = useState(false);

  const onConfigurationChange = (config: HalRepresentation, valid: boolean) => {
    setConfiguration(config);
    setIsValid(valid);
  };

  const onSubmit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (configuration) {
      update(configuration);
    }
  };

  return (
    <ConfigurationForm isValid={isValid} onSubmit={onSubmit} {...formProps}>
      {initialConfiguration
        ? render({
            readOnly: formProps.isReadOnly,
            initialConfiguration,
            onConfigurationChange,
          })
        : null}
    </ConfigurationForm>
  );
};

export default Configuration;
