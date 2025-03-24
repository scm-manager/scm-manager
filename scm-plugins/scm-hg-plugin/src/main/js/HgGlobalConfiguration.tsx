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
import { Button, ConfigurationForm, Form, Title, useDocumentTitle } from "@scm-manager/ui-core";
import { SmallLoadingSpinner, validation } from "@scm-manager/ui-components";
import { HgGlobalConfigurationDto, useHgAutoConfiguration } from "./hooks";

type Props = {
  link: string;
};

const HgGlobalConfiguration: FC<Props> = ({ link }) => {
  const { mutate: triggerAutoConfiguration, isLoading: isAutoConfigLoading } = useHgAutoConfiguration(link);
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-hg-plugin.config.title"));

  const isHgBinaryValid = (hgBinaryPath: string | undefined | null) => {
    return !hgBinaryPath || validation.isPathValid(hgBinaryPath);
  };

  return (
    <ConfigurationForm<HgGlobalConfigurationDto> link={link} translationPath={["plugins", "scm-hg-plugin.config"]}>
      {({ watch, getValues }) => (
        <>
          <Title>{t("scm-hg-plugin.config.title")}</Title>
          <Form.Row>
            <Form.Checkbox name="disabled" readOnly={!watch("allowDisable")} />
          </Form.Row>
          {!watch("disabled") ? (
            <>
              <Form.Row>
                <Form.Input name="hgBinary" rules={{ validate: isHgBinaryValid }} />
              </Form.Row>
              <Form.Row>
                <Form.Input name="encoding" />
              </Form.Row>
              <Form.Row>
                <Form.Checkbox name="showRevisionInId" />
              </Form.Row>
              <Form.Row>
                <Form.Checkbox name="enableHttpPostArgs" />
              </Form.Row>
              <Form.Row className="is-justify-content-flex-end">
                <Button className="mr-2" onClick={() => triggerAutoConfiguration(getValues())}>
                  {isAutoConfigLoading ? <SmallLoadingSpinner /> : t("scm-hg-plugin.config.autoConfigure")}
                </Button>
              </Form.Row>
            </>
          ) : null}
        </>
      )}
    </ConfigurationForm>
  );
};

export default HgGlobalConfiguration;
