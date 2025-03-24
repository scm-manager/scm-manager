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

import React, {FC, useState} from "react";
import { useTranslation } from "react-i18next";
import { InputField, Subtitle } from "@scm-manager/ui-components";
import { ConfigChangeHandler } from "@scm-manager/ui-types";

type Props = {
  jwtExpirationInH: number;
  enabledJwtEndless: boolean;
  onChange: ConfigChangeHandler;
  hasUpdatePermission: boolean;
};

const JwtSettings: FC<Props> = ({ jwtExpirationInH, onChange, hasUpdatePermission, enabledJwtEndless }) => {
  const { t } = useTranslation("config");
  const [warning, setWarning] = useState<string | undefined>(undefined);
  const jwtOverTwentyFourWarning = t("jwtSettings.hoursWarning");
  const jwtIsSetToEndless = t("jwtSettings.endlessWarning");

  const handleJwtTimeChange = (value: string) => {
    if (Number(value) > 24) {
      setWarning(jwtOverTwentyFourWarning);
    } else {
      setWarning(undefined);
    }
    onChange(true, Number(value), "jwtExpirationInH");
  };

  return (
    <div>
      <Subtitle subtitle={t("jwtSettings.subtitle")} />
      <div className="columns">
        <div className="column">
          <InputField
            type="number"
            label={t("jwtSettings.label")}
            onChange={handleJwtTimeChange}
            value={jwtExpirationInH}
            disabled={!hasUpdatePermission || enabledJwtEndless}
            helpText={t("jwtSettings.help")}
            warning={enabledJwtEndless ? jwtIsSetToEndless : warning}
          />
        </div>
      </div>
    </div>
  );
};

export default JwtSettings;
