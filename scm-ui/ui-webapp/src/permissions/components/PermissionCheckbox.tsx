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

import React, { FC, Ref } from "react";
import { useTranslation } from "react-i18next";
import { Checkbox, Field } from "@scm-manager/ui-core";

type Props = {
  name: string;
  entityType: "namespace" | "repository";
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled: boolean;
  role?: boolean;
  ref?: Ref<HTMLInputElement>;
};

const DefaultPermissionCheckbox: FC<Props> = ({ name, entityType, checked, onChange, disabled, role, ref }) => {
  const [t] = useTranslation("plugins");
  const key = name.split(":").join(".");

  const translateOrDefault = (key: string, defaultText: string) => {
    const translation = t(key);
    if (translation === key) {
      return defaultText;
    } else {
      return translation;
    }
  };

  const namespaceOrRepositoryLabel = (name: string, type: "displayName" | "description") => {
    if (entityType === "repository") {
      return t("verbs.repository." + name + "." + type);
    } else {
      return translateOrDefault("verbs.namespace." + name + "." + type, t("verbs.repository." + name + "." + type));
    }
  };

  const label = role
    ? namespaceOrRepositoryLabel(name, "displayName")
    : translateOrDefault("permissions." + key + ".displayName", key);
  const helpText = role
    ? namespaceOrRepositoryLabel(name, "description")
    : translateOrDefault("permissions." + key + ".description", t("permissions.unknown"));

  return (
    <Field>
      <Checkbox
        name={name}
        label={label}
        helpText={helpText}
        checked={checked}
        disabled={disabled}
        testId={label}
        onChange={onChange ? (event) => onChange(event.target.checked, name) : undefined}
        ref={ref}
      />
    </Field>
  );
};

const PermissionCheckbox: FC<Props> = ({ ref, ...props }) => <DefaultPermissionCheckbox {...props} ref={ref} />;

export default PermissionCheckbox;
