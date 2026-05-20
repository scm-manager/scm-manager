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

import { useLocalStorage } from "./localStorage";

export const PROFILE_CODE_STORAGE_KEY = "scm.profile.code.setting";
export const PROFILE_CODE_DEFAULT_TAB_SIZE = 4;

export type CodeSettingConfig = {
  tabSize: number;
};

export const useCodeSettingsValue = () => {
  const [value] = useLocalStorage<CodeSettingConfig>(PROFILE_CODE_STORAGE_KEY, {
    tabSize: PROFILE_CODE_DEFAULT_TAB_SIZE,
  });

  return { value, isLoading: false } as const;
};
