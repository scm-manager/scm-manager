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

import { useTranslation } from "react-i18next";
import React from "react";
import { Field, RadioGroup } from "@scm-manager/ui-core";
import { LayoutMode } from "./diffLayout";

type LayoutProps = {
  layout: LayoutMode;
  setLayout: (layout: LayoutMode) => void;
};

const LayoutRadioButtons = ({ setLayout, layout }: LayoutProps) => {
  const [t] = useTranslation("repos");

  return (
    <Field as="fieldset">
      <RadioGroup
        aria-label={t("changesets.diffTree.layout")}
        className="is-flex is-flex-wrap-wrap is-align-items-center mt-2"
        value={layout}
        onValueChange={(value) => setLayout(value as unknown as LayoutMode)}
      >
        <h3 className="mr-3 text is-6 has-text-weight-medium">{t("changesets.diffTree.layout")}</h3>
        <div className="mr-3">
          <RadioGroup.Option className="column" label={t("changesets.diffTree.hide")} value="Diff" />
        </div>
        <div className="mr-3">
          <RadioGroup.Option className="column" label={t("changesets.diffTree.show")} value="Both" />
        </div>
        <div className="mr-3">
          <RadioGroup.Option className="column" label={t("changesets.diffTree.full")} value="Tree" />
        </div>
      </RadioGroup>
    </Field>
  );
};
export default LayoutRadioButtons;
