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

import React, { useState } from "react";
import { useShortcutDocs, useShortcut } from "@scm-manager/ui-shortcuts";
import { Column, Modal, Table } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import splitKeyCombination from "./splitKeyCombination";
import styled from "styled-components";

const keyComparator = ([a]: [string, string], [b]: [string, string]) => (a > b ? 1 : -1);

const KeyDisplay = styled.span`
  border: 1px solid var(--scm-border-color);
`;

const ShortcutDocsModal = () => {
  const { docs } = useShortcutDocs();
  const [open, setOpen] = useState(false);
  const [t] = useTranslation("commons");
  useShortcut("?", () => setOpen(true), t("shortcuts.docs"));

  return (
    <Modal title={t("shortcutDocsModal.title")} closeFunction={() => setOpen(false)} active={open}>
      <p className="mb-2">{t("shortcutDocsModal.description")}</p>
      <Table data={Object.entries(docs).sort(keyComparator)}>
        <Column header={t("shortcutDocsModal.table.headers.keyCombination")}>
          {([key]: [string, string]) =>
            splitKeyCombination(key).map((k, i) => (
              <KeyDisplay
                className={classNames(
                  "has-background-secondary-less has-text-secondary-most py-1 px-2 has-rounded-border",
                  {
                    "ml-1": i > 0,
                  }
                )}
              >
                {k}
              </KeyDisplay>
            ))
          }
        </Column>
        <Column header={t("shortcutDocsModal.table.headers.description")}>
          {([, description]: [string, string]) => description}
        </Column>
      </Table>
    </Modal>
  );
};

export default ShortcutDocsModal;
