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
import { Configuration } from "@scm-manager/ui-components";
import { ConfigurationForm, Title, useDocumentTitle, Form } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";

type Props = {
  link: string;
};

type Configuration = {
  disabled: boolean;
  allowDisable: boolean;
  compatibility: string;
  enabledGZip: boolean;
} & HalRepresentation;

const SvnGlobalConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-svn-plugin.config.title"));

  const compatibilityOptions = ["NONE", "PRE14", "PRE15", "PRE16", "PRE17", "WITH17"].map((option: string) => (
    <option value={option} key={`compatibility-${option}`}>
      {t("scm-svn-plugin.config.compatibility-values." + option.toLowerCase())}
    </option>
  ));

  return (
    <ConfigurationForm<Configuration> link={link} translationPath={["plugins", "scm-svn-plugin.config"]}>
      {({ watch }) => (
        <>
          <Title>{t("scm-svn-plugin.config.title")}</Title>
          <Form.Row>
            <Form.Checkbox name="disabled" readOnly={!watch("allowDisable")} />
          </Form.Row>
          {!watch("disabled") ? (
            <>
              <Form.Row>
                <Form.Select name="compatibility">{compatibilityOptions}</Form.Select>
              </Form.Row>
              <Form.Row>
                <Form.Checkbox name="enabledGZip" />
              </Form.Row>
            </>
          ) : null}
        </>
      )}
    </ConfigurationForm>
  );
};

export default SvnGlobalConfiguration;
