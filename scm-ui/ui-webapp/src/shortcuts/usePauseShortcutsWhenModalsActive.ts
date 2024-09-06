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

import { useActiveModals } from "@scm-manager/ui-components";
import { usePauseShortcuts } from "@scm-manager/ui-shortcuts";
import { useAccessibilityConfig } from "../accessibilityConfig";

/**
 * Keyboard shortcuts are not active in modals using {@link useActiveModals} to determine whether any modals are open.
 *
 * Has to be used inside a {@link ActiveModalCountContextProvider}.
 */
export default function usePauseShortcutsWhenModalsActive() {
  const areModalsActive = useActiveModals();
  const {
    value: { deactivateShortcuts },
  } = useAccessibilityConfig();
  usePauseShortcuts(deactivateShortcuts || areModalsActive);
}
