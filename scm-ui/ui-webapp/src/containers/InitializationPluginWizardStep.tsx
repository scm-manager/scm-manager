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
import { HalRepresentationWithEmbedded, Links } from "@scm-manager/ui-types";
import { useQuery } from "react-query";
import { apiClient, ApiResult, requiredLink } from "@scm-manager/ui-api";

type PluginSet = {
  id: string;
  name: string;
  sequence: number;
  features: string[];
  plugins: string[];
};

type AvailablePluginSets = HalRepresentationWithEmbedded<{ pluginSets: PluginSet[] }>;

type Props = {
  data: { _links: Links };
};

const useAvailablePluginSets = (link: string): ApiResult<AvailablePluginSets> =>
  useQuery<AvailablePluginSets, Error>("pluginSets", () => apiClient.get(link).then((r) => r.json()));

const InitializationPluginWizardStep: FC<Props> = ({ data }) => {
  const { data: pluginSets, isLoading, error } = useAvailablePluginSets(requiredLink(data, "pluginSets"));
  const [t] = useTranslation("initialization");
  return (
    <>
      <h1>Hello World</h1>
      {pluginSets?._embedded?.pluginSets.map((pluginSet) => (
        <div>{pluginSet.name}</div>
      ))}
    </>
  );
};

export default InitializationPluginWizardStep;
