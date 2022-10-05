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
import React, { useCallback, useMemo, useState } from "react";
import useShortcutDocs from "./useShortcutDocs";
import { Column, Modal, Table } from "@scm-manager/ui-components";
import useShortcut from "./useShortcut";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import splitKeyCombination from "./splitKeyCombination";

const keyComparator = ([a]: [string, string], [b]: [string, string]) => (a > b ? 1 : -1);

const ShortcutDocsModal = () => {
  const { docs } = useShortcutDocs();
  const [open, setOpen] = useState(false);
  const [t] = useTranslation("commons");
  const entries = useMemo(() => Object.entries(docs).sort(keyComparator), [docs]);
  const openModal = useCallback(() => setOpen(true), []);
  useShortcut("?", openModal, t("shortcuts.docs"));

  return (
    <Modal title={t("shortcutDocsModal.title")} closeFunction={() => setOpen(false)} active={open}>
      <p className="mb-2">{t("shortcutDocsModal.description")}</p>
      <Table data={entries}>
        <Column header={t("shortcutDocsModal.table.headers.keyCombination")}>
          {([key]: [string, string]) =>
            splitKeyCombination(key).map((k, i) => (
              <span
                className={classNames(
                  "has-background-secondary-less has-text-secondary-most py-1 px-2 has-rounded-border",
                  {
                    "ml-1": i > 0,
                  }
                )}
              >
                {k}
              </span>
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
