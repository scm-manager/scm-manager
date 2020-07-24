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
import { User, Link, Links, Collection } from "@scm-manager/ui-types/src";
import {
  ErrorNotification,
  InputField,
  Level,
  Textarea,
  SubmitButton,
  apiClient,
  Loading
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { CONTENT_TYPE_PUBLIC_KEY } from "./SetPublicKeys";

type Props = {
  createLink: string;
  refresh: () => void;
};

const AddPublicKey: FC<Props> = ({ createLink, refresh }) => {
  const [t] = useTranslation("users");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<undefined | Error>();
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
    setLoading(true);
    apiClient
      .post(createLink, { displayName: displayName, raw: raw }, CONTENT_TYPE_PUBLIC_KEY)
      .then(resetForm)
      .then(refresh)
      .then(() => setLoading(false))
      .catch(setError);
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <InputField label={t("publicKey.displayName")} value={displayName} onChange={setDisplayName} />
      <Textarea name="raw" label={t("publicKey.raw")} value={raw} onChange={setRaw} />
      <Level
        right={<SubmitButton label={t("publicKey.addKey")} loading={loading} disabled={!isValid()} action={addKey} />}
      />
    </>
  );
};

export default AddPublicKey;
