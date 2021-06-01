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
