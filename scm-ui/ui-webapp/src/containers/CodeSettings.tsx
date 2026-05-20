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
import { Button, Input, Label, Level, Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { CodeSettingConfig, useRequiredMe } from "@scm-manager/ui-api";
import { useCodeSettingsConfig } from "../codeSettingsConfig";
import styled from "styled-components";

const StyledInput = styled(Input)`
  margin-top: 6px;
  width: 55px;
`;

const CodeContainer = styled.pre`
  margin-top: 6px;
  padding: 1rem;
  border-radius: 4px;
  border: 1px solid #ddd;
`;

const previewCode =
  "function binarySearch(arr, target) {\n" +
  "\tlet left = 0;\n" +
  "\tlet right = arr.length - 1;\n" +
  "\n" +
  "\twhile (left <= right) {\n" +
  "\t\tlet mid = left + Math.floor((right - left) / 2);\n" +
  "\n" +
  "\t\tif (arr[mid] === target) {\n" +
  "\t\t\treturn mid; \n" +
  "\t\t}\n" +
  "\n" +
  "\t\tif (arr[mid] < target) {\n" +
  "\t\t\tleft = mid + 1;\n" +
  "\t\t} else {\n" +
  "\t\t\tright = mid - 1;\n" +
  "\t\t}\n" +
  "\t}\n" +
  "\n" +
  "\treturn -1;\n" +
  "}";

const CodeSettings: FC = () => {
  const [t] = useTranslation("commons");
  const me = useRequiredMe();
  const displayName = me?.displayName ?? "";
  useDocumentTitle(t("profile.code.subtitle"), displayName);

  const { value: accessibilityConfig, setValue: setAccessibilityConfig, isLoading } = useCodeSettingsConfig();
  const {
    register,
    handleSubmit,
    watch,
    formState: { isDirty },
  } = useForm<CodeSettingConfig>({
    mode: "onChange",
    defaultValues: accessibilityConfig,
  });
  const currentTabSize = watch("tabSize") || 4;

  return (
    <>
      <Subtitle>{t("profile.code.subtitle")}</Subtitle>
      <form onSubmit={handleSubmit(setAccessibilityConfig)}>
        <div className="mb-5">
          <Label htmlFor="tabSize-input">{t("profile.code.label.tabSize")}</Label>
          <StyledInput id="tabSize-input" type="number" min="2" max="8" {...register("tabSize")} />
        </div>
        <div className="my-3">
          <Label htmlFor="tabSize-preview">{t("profile.code.label.preview")}</Label>
          <CodeContainer
            id="tabSize-preview"
            style={{
              tabSize: currentTabSize,
              MozTabSize: currentTabSize,
              whiteSpace: "pre",
            }}
          >
            {previewCode}
          </CodeContainer>
        </div>
        <Level
          right={
            <Button type="submit" variant="primary" isLoading={isLoading} disabled={!isDirty}>
              {t("profile.code.submit")}
            </Button>
          }
        />
      </form>
    </>
  );
};

export default CodeSettings;
