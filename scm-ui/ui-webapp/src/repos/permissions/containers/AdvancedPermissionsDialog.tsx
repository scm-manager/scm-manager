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

import React, { FC, useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { Button, ButtonGroup, Modal, SubmitButton } from "@scm-manager/ui-components";
import PermissionCheckbox from "../../../permissions/components/PermissionCheckbox";

type SelectedVerbs = { [verb: string]: boolean };

type Props = {
  availableVerbs: string[];
  selectedVerbs: string[];
  readOnly: boolean;
  onSubmit: (verbs: string[]) => void;
  onClose: () => void;
  entityType: "namespace" | "repository";
};

const createPreSelection = (availableVerbs: string[], selectedVerbs?: string[]): SelectedVerbs => {
  const verbs: SelectedVerbs = {};
  availableVerbs.forEach(verb => (verbs[verb] = selectedVerbs ? selectedVerbs.includes(verb) : false));
  return verbs;
};

const AdvancedPermissionsDialog: FC<Props> = ({ availableVerbs, selectedVerbs, entityType, readOnly, onSubmit, onClose }) => {
  const [verbs, setVerbs] = useState<SelectedVerbs>({});
  const [t] = useTranslation("repos");
  const initialFocusRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setVerbs(createPreSelection(availableVerbs, selectedVerbs));
  }, [availableVerbs, selectedVerbs]);

  const handleChange = (value: boolean, name?: string) => {
    if (name) {
      setVerbs({
        ...verbs,
        [name]: value
      });
    }
  };

  const verbSelectBoxes = Object.entries(verbs).map(([name, checked], index) => (
    <PermissionCheckbox
      key={name}
      name={name}
      checked={checked}
      onChange={handleChange}
      disabled={readOnly}
      role={true}
      entityType={entityType}
      ref={index === 0 ? initialFocusRef : undefined}
    />
  ));

  const handleSubmit = () => {
    onSubmit(
      Object.entries(verbs)
        .filter(([_, checked]) => checked)
        .map(([name]) => name)
    );
  };

  const footer = (
    <form onSubmit={handleSubmit}>
      <ButtonGroup>
        {!readOnly ? <SubmitButton label={t("permission.advanced.dialog.submit")} /> : null}
        <Button label={t("permission.advanced.dialog.abort")} action={onClose} />
      </ButtonGroup>
    </form>
  );

  return (
    <Modal
      title={t("permission.advanced.dialog.title")}
      closeFunction={() => onClose()}
      body={<>{verbSelectBoxes}</>}
      footer={footer}
      active={true}
      initialFocusRef={initialFocusRef}
    />
  );
};

export default AdvancedPermissionsDialog;
