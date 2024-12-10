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
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useConfigLink } from "@scm-manager/ui-api";
import { ConfigurationForm, InputField, Checkbox, validation } from "@scm-manager/ui-components";
import { Title, useDocumentTitle } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";

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
  useDocumentTitle(t("scm-git-plugin.config.title"));

  const { initialConfiguration, isReadOnly, update, ...formProps } = useConfigLink<Configuration>(link);
  const { formState, handleSubmit, register, reset } = useForm<Configuration>({ mode: "onChange" });

  useEffect(() => {
    if (initialConfiguration) {
      reset(initialConfiguration);
    }
  }, [initialConfiguration, reset]);

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
      <Title>{t("scm-git-plugin.config.title")}</Title>
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
