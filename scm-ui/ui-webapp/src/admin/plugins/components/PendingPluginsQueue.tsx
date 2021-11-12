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
import { PendingPlugins } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

type Props = {
  pendingPlugins: PendingPlugins;
};

type SectionProps = Props & {
  type: string;
  label: string;
};

const Section: FC<SectionProps> = ({ pendingPlugins, type, label }) => {
  const plugins = pendingPlugins?._embedded[type];
  if (!plugins || plugins.length === 0) {
    return null;
  }
  return (
    <>
      <strong>{label}</strong>
      <ul>
        {plugins.map((plugin) => (
          <li key={plugin.name}>{plugin.name}</li>
        ))}
      </ul>
    </>
  );
};

const PendingPluginsQueue: FC<Props> = ({ pendingPlugins }) => {
  const [t] = useTranslation("admin");
  return (
    <>
      <Section pendingPlugins={pendingPlugins} type="new" label={t("plugins.modal.installQueue")} />
      <Section pendingPlugins={pendingPlugins} type="update" label={t("plugins.modal.updateQueue")} />
      <Section pendingPlugins={pendingPlugins} type="uninstall" label={t("plugins.modal.uninstallQueue")} />
    </>
  );
};

export default PendingPluginsQueue;
