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
    watch,
  } = useForm<ThemeForm>({
    mode: "onChange",
    defaultValues: {
      theme,
    },
  });
  const [t] = useTranslation("commons");

  const onSubmit = (values: ThemeForm) => {
    setTheme(values.theme);
  };

  return (
    <>
      <Subtitle>{t("profile.theme.subtitle")}</Subtitle>
      <form className="is-flex is-flex-direction-column" onSubmit={handleSubmit(onSubmit)}>
        {themes.map((theme) => {
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
