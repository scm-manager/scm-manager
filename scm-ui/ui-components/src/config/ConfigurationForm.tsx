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

import React, { FC, FormEvent, useEffect, useState } from "react";
import { ErrorNotification, Level, SubmitButton } from "../index";
import { useTranslation } from "react-i18next";
import Loading from "../Loading";

type Props = {
  isUpdating: boolean;
  isUpdated: boolean;
  isReadOnly: boolean;
  isLoading: boolean;
  isValid: boolean;
  error?: Error | null;
  onSubmit: (e: FormEvent<HTMLFormElement>) => void;
};

const ConfigurationChangedNotification: FC<{ configChanged: boolean }> = ({ configChanged }) => {
  const [t] = useTranslation("config");
  const [hide, setHide] = useState(false);
  useEffect(() => {
    setHide(false);
  }, [configChanged]);

  if (!configChanged || hide) {
    return null;
  }
  return (
    <div className="notification is-primary">
      <button className="delete" onClick={() => setHide(true)} />
      {t("config.form.submit-success-notification")}
    </div>
  );
};

const ConfigurationForm: FC<Props> = ({
  isLoading,
  isReadOnly,
  isValid,
  isUpdating,
  isUpdated,
  error,
  onSubmit,
  children,
}) => {
  const [t] = useTranslation("config");

  if (isLoading) {
    return <Loading />;
  }

  return (
    <form onSubmit={onSubmit}>
      <ConfigurationChangedNotification configChanged={isUpdated} />
      {children}
      {error && (
        <>
          <hr />
          <ErrorNotification error={error} />
        </>
      )}
      <hr />
      <Level
        right={<SubmitButton label={t("config.form.submit")} disabled={!isValid || isReadOnly} loading={isUpdating} />}
      />
    </form>
  );
};

export default ConfigurationForm;
