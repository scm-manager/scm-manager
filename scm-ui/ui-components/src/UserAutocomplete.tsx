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
import UserGroupAutocomplete, { AutocompleteProps } from "./UserGroupAutocomplete";

/**
 * @deprecated
 * @since 2.45.0
 *
 * Use {@link Combobox} instead
 */
const UserAutocomplete: FC<AutocompleteProps> = (props) => {
  const [t] = useTranslation("commons");
  return (
    <UserGroupAutocomplete
      label={t("autocomplete.user")}
      noOptionsMessage={t("autocomplete.noUserOptions")}
      loadingMessage={t("autocomplete.loading")}
      placeholder={t("autocomplete.userPlaceholder")}
      {...props}
    />
  );
};

export default UserAutocomplete;
