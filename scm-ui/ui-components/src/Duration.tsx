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

type Unit = "ms" | "s" | "m" | "h" | "d" | "w";

type Props = {
  duration: number;
};

export const parse = (duration: number) => {
  let value = duration;
  let unit: Unit = "ms";
  if (value > 1000) {
    unit = "s";
    value /= 1000;
    if (value > 60) {
      unit = "m";
      value /= 60;
      if (value > 60) {
        unit = "h";
        value /= 60;
        if (value > 24) {
          unit = "d";
          value /= 24;
          if (value > 7) {
            unit = "w";
            value /= 7;
          }
        }
      }
    }
  }
  return {
    duration: Math.round(value),
    unit,
  };
};

const Duration: FC<Props> = ({ duration }) => {
  const [t] = useTranslation("commons");
  const parsed = parse(duration);
  return (
    <time dateTime={`${parsed.duration}${parsed.unit}`}>
      {t(`duration.${parsed.unit}`, { count: parsed.duration })}
    </time>
  );
};

export default Duration;
