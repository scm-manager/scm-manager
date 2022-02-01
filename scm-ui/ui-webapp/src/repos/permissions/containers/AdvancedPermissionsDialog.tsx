/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
};

const createPreSelection = (availableVerbs: string[], selectedVerbs?: string[]): SelectedVerbs => {
  const verbs: SelectedVerbs = {};
  availableVerbs.forEach(verb => (verbs[verb] = selectedVerbs ? selectedVerbs.includes(verb) : false));
  return verbs;
};

const AdvancedPermissionsDialog: FC<Props> = ({ availableVerbs, selectedVerbs, readOnly, onSubmit, onClose }) => {
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
