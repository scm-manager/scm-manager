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

import React, { FC, KeyboardEvent, useState } from "react";
import { useController, useForm } from "react-hook-form";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Button, RadioGroup, Subtitle, useDocumentTitle } from "@scm-manager/ui-core";
import { useRequiredMe } from "@scm-manager/ui-api";

const LS_KEY = "scm.theme";

export const useThemeState = () => {
  const [theme] = useState(localStorage.getItem(LS_KEY) || "systemdefault");
  const [isLoading, setLoading] = useState(false);

  const setTheme = (name: string) => {
    setLoading(true);
    localStorage.setItem(LS_KEY, name);
    window.location.reload();
  };

  return { theme, setTheme, isLoading };
};

type ThemeForm = {
  theme: string;
};

const themes = ["light", "dark", "highcontrast", "systemdefault"];

const RadioColumn = styled.div`
  flex: none;
  width: 2rem;
`;

const Theme: FC = () => {
  const { theme, setTheme, isLoading } = useThemeState();
  const me = useRequiredMe();
  const {
    control,
    handleSubmit,
    formState: { isDirty },
  } = useForm<ThemeForm>({
    mode: "onChange",
    defaultValues: {
      theme,
    },
  });
  const { field } = useController({ control, name: "theme" });
  const [t] = useTranslation("commons");
  useDocumentTitle(t("profile.theme.subtitle"), me.displayName);

  const onSubmit = (values: ThemeForm) => {
    window.scrollTo(0, 0);
    setTheme(values.theme);
  };

  // Radix prevents the default of the enter key on radio items, which blocks the implicit submission of the form
  const submitOnEnter = (event: KeyboardEvent<HTMLButtonElement>) => {
    if (event.key === "Enter" && isDirty) {
      handleSubmit(onSubmit)();
    }
  };

  return (
    <>
      <Subtitle>{t("profile.theme.subtitle")}</Subtitle>
      <form className="is-flex is-flex-direction-column" onSubmit={handleSubmit(onSubmit)}>
        <RadioGroup
          name={field.name}
          value={field.value}
          onValueChange={field.onChange}
          disabled={isLoading}
          aria-label={t("profile.theme.subtitle")}
        >
          {themes.map((theme) => {
            const a11yId = `theme-${theme}`;
            return (
              <div
                key={theme}
                onClick={() => field.onChange(theme)}
                className={classNames(
                  "card",
                  "ml-1",
                  "mb-5",
                  "control",
                  "columns",
                  "is-vcentered",
                  "has-cursor-pointer",
                  { "has-background-secondary-less": theme === field.value },
                )}
              >
                <RadioColumn className="column">
                  <RadioGroup.Option value={theme} label="" aria-labelledby={a11yId} onKeyDown={submitOnEnter} />
                </RadioColumn>
                <div id={a11yId} className="column content">
                  <h3>{t(`profile.theme.${theme}.displayName`)}</h3>
                  <p>{t(`profile.theme.${theme}.description`)}</p>
                </div>
              </div>
            );
          })}
        </RadioGroup>
        <div className="buttons is-justify-content-flex-end">
          <Button type="submit" variant="primary" isLoading={isLoading} disabled={!isDirty} testId="submit-button">
            {t("profile.theme.submit")}
          </Button>
        </div>
      </form>
    </>
  );
};

export default Theme;
