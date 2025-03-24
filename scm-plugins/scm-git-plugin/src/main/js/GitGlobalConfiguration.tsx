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
import { ConfigurationForm, Form, Title, useDocumentTitle } from "@scm-manager/ui-core";
import { HalRepresentation } from "@scm-manager/ui-types";
import { validation } from "@scm-manager/ui-components";

type Props = {
  link: string;
};

type Configuration = HalRepresentation & {
  disabled: boolean;
  allowDisable: boolean;
  repositoryDirectory?: string;
  gcExpression?: string;
  nonFastForwardDisallowed: boolean;
  defaultBranch: string;
  lfsWriteAuthorizationExpirationInMinutes: number;
};

const GitGlobalConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-git-plugin.config.title"));

  const validateLfsWriteAuthorization = (value: string) => {
    const authorizationTime = parseInt(value);
    return Number.isInteger(authorizationTime) && authorizationTime > 0;
  };

  return (
    <ConfigurationForm<Configuration> link={link} translationPath={["plugins", "scm-git-plugin.config"]}>
      {({ watch }) => (
        <>
          <Title>{t("scm-git-plugin.config.title")}</Title>
          <Form.Row>
            <Form.Checkbox name="disabled" readOnly={!watch("allowDisable")} />
          </Form.Row>
          {!watch("disabled") ? (
            <>
              <Form.Row>
                <Form.Input name="gcExpression" />
              </Form.Row>
              <Form.Row>
                <Form.Checkbox name="nonFastForwardDisallowed" />
              </Form.Row>
              <Form.Row>
                <Form.Input name="defaultBranch" rules={{ required: true, validate: validation.isBranchValid }} />
              </Form.Row>
              <Form.Row>
                <Form.Input
                  name="lfsWriteAuthorizationExpirationInMinutes"
                  type="number"
                  rules={{ required: true, validate: validateLfsWriteAuthorization }}
                />
              </Form.Row>
            </>
          ) : null}
        </>
      )}
    </ConfigurationForm>
  );
};

export default GitGlobalConfiguration;
