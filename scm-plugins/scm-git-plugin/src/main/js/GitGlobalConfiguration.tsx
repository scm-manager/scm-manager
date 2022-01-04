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
