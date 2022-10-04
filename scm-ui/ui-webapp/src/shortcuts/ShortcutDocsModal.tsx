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

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import React, { useState } from "react";
import useShortcutDocs from "./useShortcutDocs";
import { Column, Modal, Table } from "@scm-manager/ui-components";
import useShortcut from "./useShortcut";
import { useTranslation } from "react-i18next";

const ShortcutDocsModal = () => {
  const { get } = useShortcutDocs();
  const [open, setOpen] = useState(false);
  const [t] = useTranslation("commons");
  useShortcut("?", () => setOpen(true), t("shortcuts.docs"));

  return (
    <Modal title={t("shortcuts.title")} closeFunction={() => setOpen(false)} active={open}>
      <p className="mb-2">{t("shortcuts.description")}</p>
      <Table data={Object.entries(get())}>
        <Column header={t("shortcuts.table.headers.keyCombination")}>
          {([key]: [string, string]) => <>{prettifyKey(key)}</>}
        </Column>
        <Column header={t("shortcuts.table.headers.description")}>
          {([, description]: [string, string]) => description}
        </Column>
      </Table>
    </Modal>
  );
};

export default ShortcutDocsModal;

function prettifyKey(key: string) {
  return key;
}
