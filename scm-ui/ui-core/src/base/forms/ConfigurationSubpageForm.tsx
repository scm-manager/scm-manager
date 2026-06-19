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

import { useConfigLink } from "@scm-manager/ui-api";
import { Loading } from "../misc";
import React, { ComponentProps } from "react";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { DefaultValues, FieldValues } from "react-hook-form";
import SubpageForm from "./subpage/SubpageForm";

type Props<T extends HalRepresentation> =
  // eslint-disable-next-line prettier/prettier
  Omit<ComponentProps<typeof SubpageForm<T>>, "onSubmit" | "defaultValues" | "readOnly" | "successMessageFallback"> & {
    link: string;
  };

/**
 * Config-link based variant of `SubpageForm`. Loads and saves the configuration behind the given
 * HAL link. Each subpage saves the whole configuration object.
 *
 * This is an additional variant and does not replace `ConfigurationForm`.
 *
 * @beta
 * @since 4.0.0
 */
export function ConfigurationSubpageForm<T extends HalRepresentation & FieldValues>({
  link,
  children,
  ...formProps
}: Props<T>) {
  const { initialConfiguration, isReadOnly, update, isLoading } = useConfigLink<T>(link);
  const [t] = useTranslation("commons", { keyPrefix: "form" });

  if (isLoading || !initialConfiguration) {
    return <Loading />;
  }

  return (
    <SubpageForm
      onSubmit={update}
      defaultValues={initialConfiguration as DefaultValues<T>}
      readOnly={isReadOnly}
      successMessageFallback={t("submit-success-notification")}
      {...formProps}
    >
      {children}
    </SubpageForm>
  );
}

export default ConfigurationSubpageForm;
