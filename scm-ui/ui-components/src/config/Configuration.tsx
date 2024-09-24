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
  render: (props: RenderProps) => any; // ???
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
