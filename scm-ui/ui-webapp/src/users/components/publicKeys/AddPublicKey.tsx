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

import React, { FC, useState } from "react";
import { ErrorNotification, InputField, Level, SubmitButton, Subtitle, Textarea } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Me, PublicKeysCollection, User } from "@scm-manager/ui-types";
import { useCreatePublicKey } from "@scm-manager/ui-api";

type Props = {
  publicKeys: PublicKeysCollection;
  user: User | Me;
};

const AddPublicKey: FC<Props> = ({ user, publicKeys }) => {
  const [t] = useTranslation("users");
  const { isLoading, error, create } = useCreatePublicKey(user, publicKeys);
  const [displayName, setDisplayName] = useState("");
  const [raw, setRaw] = useState("");

  const isValid = () => {
    return !!displayName && !!raw;
  };

  const resetForm = () => {
    setDisplayName("");
    setRaw("");
  };

  const addKey = () => {
    create({ raw, displayName });
    resetForm();
  };

  return (
    <>
      <hr />
      {error ? <ErrorNotification error={error} /> : null}
      <Subtitle subtitle={t("publicKey.addSubtitle")} />
      <InputField label={t("publicKey.displayName")} value={displayName} onChange={setDisplayName} />
      <Textarea name="raw" label={t("publicKey.raw")} value={raw} onChange={setRaw} />
      <Level
        right={
          <SubmitButton
            label={t("publicKey.addKey")}
            loading={isLoading}
            disabled={!isValid() || isLoading}
            action={addKey}
          />
        }
      />
    </>
  );
};

export default AddPublicKey;
