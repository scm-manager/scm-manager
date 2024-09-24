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

import { useLocalStorage } from "@scm-manager/ui-api";
import { useCallback, useState } from "react";

const LOCAL_STORAGE_KEY = "scm.accessibility";

export type AccessibilityConfig = {
  deactivateShortcuts: boolean;
};

export const useAccessibilityConfig = () => {
  const [value, updateConfig] = useLocalStorage<AccessibilityConfig>(LOCAL_STORAGE_KEY, { deactivateShortcuts: false });
  const [isLoading, setLoading] = useState(false);
  const setValue = useCallback(
    (newConfig: AccessibilityConfig) => {
      setLoading(true);
      updateConfig(newConfig);
      window.location.reload();
    },
    [updateConfig]
  );
  return { value, setValue, isLoading } as const;
};
