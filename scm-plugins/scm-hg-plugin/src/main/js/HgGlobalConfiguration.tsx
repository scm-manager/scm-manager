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
import { Title, useDocumentTitle } from "@scm-manager/ui-core";
import HgConfigurationForm from "./HgConfigurationForm";

type Props = {
  link: string;
};

const HgGlobalConfiguration: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  useDocumentTitle(t("scm-hg-plugin.config.title"));

  return (
    <div>
      <Title>{t("scm-hg-plugin.config.title")}</Title>
      <Configuration link={link} render={(props: any) => <HgConfigurationForm {...props} />} />
    </div>
  );
};

export default HgGlobalConfiguration;
