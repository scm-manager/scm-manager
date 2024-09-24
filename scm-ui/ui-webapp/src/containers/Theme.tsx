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

import React, { FC, useState } from "react";
import { ButtonGroup, createA11yId, Radio, SubmitButton, Subtitle } from "@scm-manager/ui-components";
import { useForm } from "react-hook-form";
import styled from "styled-components";
import { useTranslation } from "react-i18next";
import classNames from "classnames";

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
  const {
    register,
    setValue,
    handleSubmit,
    formState: { isDirty },
    watch
  } = useForm<ThemeForm>({
    mode: "onChange",
    defaultValues: {
      theme
    }
  });
  const [t] = useTranslation("commons");

  const onSubmit = (values: ThemeForm) => {
    setTheme(values.theme);
  };

  return (
    <>
      <Subtitle>{t("profile.theme.subtitle")}</Subtitle>
      <form className="is-flex is-flex-direction-column" onSubmit={handleSubmit(onSubmit)}>
        {themes.map(theme => {
          const a11yId = createA11yId("theme");
          return (
            <div
              key={theme}
              onClick={() => setValue("theme", theme, { shouldDirty: true })}
              className={classNames(
                "card",
                "ml-1",
                "mb-5",
                "control",
                "columns",
                "is-vcentered",
                "has-cursor-pointer",
                { "has-background-secondary-less": theme === watch().theme }
              )}
            >
              <RadioColumn className="column">
                <Radio {...register("theme")} value={theme} disabled={isLoading} ariaLabelledby={a11yId} />
              </RadioColumn>
              <div id={a11yId} className="column content">
                <h3>{t(`profile.theme.${theme}.displayName`)}</h3>
                <p>{t(`profile.theme.${theme}.description`)}</p>
              </div>
            </div>
          );
        })}
        <ButtonGroup className="is-justify-content-flex-end">
          <SubmitButton label={t("profile.theme.submit")} loading={isLoading} disabled={!isDirty} />
        </ButtonGroup>
      </form>
    </>
  );
};

export default Theme;
