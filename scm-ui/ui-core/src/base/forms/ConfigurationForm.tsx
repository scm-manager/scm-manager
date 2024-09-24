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
import Form from "./Form";
import { useTranslation } from "react-i18next";

type Props<T extends HalRepresentation> =
// eslint-disable-next-line prettier/prettier
  Omit<ComponentProps<typeof Form<T>>, "onSubmit" | "defaultValues" | "readOnly" | "successMessageFallback">
  & {
  link: string;
};

/**
 * @beta
 * @since 2.41.0
 */
export function ConfigurationForm<T extends HalRepresentation>({ link, children, ...formProps }: Props<T>) {
  const { initialConfiguration, isReadOnly, update, isLoading } = useConfigLink<T>(link);
  const [t] = useTranslation("commons", { keyPrefix: "form" });

  if (isLoading || !initialConfiguration) {
    return <Loading />;
  }

  return (
    <Form onSubmit={update} defaultValues={initialConfiguration} readOnly={isReadOnly}
          successMessageFallback={t("submit-success-notification")} {...formProps}>
      {children}
    </Form>
  );
}

export default ConfigurationForm;
