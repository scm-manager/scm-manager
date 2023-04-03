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

import { useConfigLink } from "@scm-manager/ui-api";
import { Loading } from "@scm-manager/ui-components";
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
