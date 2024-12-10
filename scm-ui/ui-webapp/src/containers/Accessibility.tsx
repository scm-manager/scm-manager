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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import { useForm } from "react-hook-form";
import { Me } from "@scm-manager/ui-types";
import { useDocumentTitle } from "@scm-manager/ui-core";
import { ButtonGroup, Checkbox, SubmitButton, Subtitle } from "@scm-manager/ui-components";
import { AccessibilityConfig, useAccessibilityConfig } from "../accessibilityConfig";

type Props = {
  me: Me;
};

const Accessibility: FC<Props> = ({ me }) => {
  const [t] = useTranslation("commons");
  useDocumentTitle(t("profile.accessibility.subtitle"), me.displayName);
  const { value: accessibilityConfig, setValue: setAccessibilityConfig, isLoading } = useAccessibilityConfig();
  const {
    register,
    handleSubmit,
    formState: { isDirty },
  } = useForm<AccessibilityConfig>({
    mode: "onChange",
    defaultValues: accessibilityConfig,
  });

  return (
    <>
      <Subtitle>{t("profile.accessibility.subtitle")}</Subtitle>
      <form onSubmit={handleSubmit(setAccessibilityConfig)}>
        <Checkbox
          label={t("profile.accessibility.deactivateShortcuts.label")}
          helpText={t("profile.accessibility.deactivateShortcuts.helpText")}
          {...register("deactivateShortcuts")}
        />
        <ButtonGroup className="is-justify-content-flex-end">
          <SubmitButton label={t("profile.accessibility.submit")} disabled={!isDirty} loading={isLoading} />
        </ButtonGroup>
      </form>
    </>
  );
};

export default Accessibility;
