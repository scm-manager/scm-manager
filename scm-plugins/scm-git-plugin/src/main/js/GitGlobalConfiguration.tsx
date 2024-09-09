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

import React, { FC, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Title, ConfigurationForm, InputField, Checkbox, validation } from "@scm-manager/ui-components";
import { useConfigLink } from "@scm-manager/ui-api";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useForm } from "react-hook-form";

type Props = {
  link: string;
};

type Configuration = HalRepresentation & {
  disabled: boolean;
  allowDisable: boolean;
  repositoryDirectory?: string;
  gcExpression?: string;
  nonFastForwardDisallowed: boolean;
  defaultBranch: string;
  lfsWriteAuthorizationExpirationInMinutes: number;
};

const GitGlobalConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");

  const { initialConfiguration, isReadOnly, update, ...formProps } = useConfigLink<Configuration>(link);
  const { formState, handleSubmit, register, reset } = useForm<Configuration>({ mode: "onChange" });

  useEffect(() => {
    if (initialConfiguration) {
      reset(initialConfiguration);
    }
  }, [initialConfiguration]);

  const isValidDefaultBranch = (value: string) => {
    return validation.isBranchValid(value);
  };

  return (
    <ConfigurationForm
      isValid={formState.isValid}
      isReadOnly={isReadOnly}
      onSubmit={handleSubmit(update)}
      {...formProps}
    >
      <Title title={t("scm-git-plugin.config.title")} />
      <InputField
        label={t("scm-git-plugin.config.gcExpression")}
        helpText={t("scm-git-plugin.config.gcExpressionHelpText")}
        disabled={isReadOnly}
        {...register("gcExpression")}
      />
      <Checkbox
        label={t("scm-git-plugin.config.nonFastForwardDisallowed")}
        helpText={t("scm-git-plugin.config.nonFastForwardDisallowedHelpText")}
        disabled={isReadOnly}
        {...register("nonFastForwardDisallowed")}
      />
      <InputField
        label={t("scm-git-plugin.config.defaultBranch")}
        helpText={t("scm-git-plugin.config.defaultBranchHelpText")}
        disabled={isReadOnly}
        validationError={!!formState.errors.defaultBranch}
        errorMessage={t("scm-git-plugin.config.defaultBranchValidationError")}
        {...register("defaultBranch", { validate: isValidDefaultBranch })}
      />
      <InputField
        type="number"
        label={t("scm-git-plugin.config.lfsWriteAuthorizationExpirationInMinutes")}
        helpText={t("scm-git-plugin.config.lfsWriteAuthorizationExpirationInMinutesHelpText")}
        disabled={isReadOnly}
        validationError={!!formState.errors.lfsWriteAuthorizationExpirationInMinutes}
        errorMessage={t("scm-git-plugin.config.lfsWriteAuthorizationExpirationInMinutesValidationError")}
        {...register("lfsWriteAuthorizationExpirationInMinutes", { min: 1, required: true })}
      />
      <Checkbox
        label={t("scm-git-plugin.config.disabled")}
        helpText={t("scm-git-plugin.config.disabledHelpText")}
        disabled={isReadOnly || !initialConfiguration?.allowDisable}
        {...register("disabled")}
      />
    </ConfigurationForm>
  );
};

export default GitGlobalConfiguration;
