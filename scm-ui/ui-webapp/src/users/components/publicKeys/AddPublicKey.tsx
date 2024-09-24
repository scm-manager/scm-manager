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
