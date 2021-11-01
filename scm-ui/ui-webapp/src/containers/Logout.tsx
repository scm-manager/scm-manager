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
import React, { FC, useEffect } from "react";
import { useTranslation } from "react-i18next";

import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { Redirect } from "react-router-dom";
import { useLogout } from "@scm-manager/ui-api";

const Logout: FC = ({}) => {
  const { error, logout } = useLogout();
  const [t] = useTranslation("commons");
  useEffect(() => {
    if (logout) {
      logout();
    }
  }, [logout]);

  if (!logout) {
    return <Redirect to={"/login"} />;
  }

  if (error) {
    return <ErrorPage title={t("logout.error.title")} subtitle={t("logout.error.subtitle")} error={error} />;
  }
  return <Loading />;
};

export default Logout;
