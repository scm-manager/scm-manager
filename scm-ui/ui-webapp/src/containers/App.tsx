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
import React, { FC } from "react";
import Main from "./Main";
import { useTranslation} from "react-i18next";
import { ErrorPage, Footer, Header, Loading, PrimaryNavigation } from "@scm-manager/ui-components";
import Login from "./Login";
import { useSubject, useIndex} from "@scm-manager/ui-api";

const App: FC = () => {
  const { data: index } = useIndex();
  const { isLoading, error, isAuthenticated, me } = useSubject();
  const [t] = useTranslation("commons");

  if (!index) {
    return null;
  }

  let content;
  const navigation = isAuthenticated ? <PrimaryNavigation links={index._links} /> : "";


  if (!isAuthenticated && !isLoading) {
    content = <Login />;
  } else if (isLoading) {
    content = <Loading />;
  } else if (error) {
    content = <ErrorPage title={t("app.error.title")} subtitle={t("app.error.subtitle")} error={error} />;
  } else if (me) {
    content = <Main authenticated={isAuthenticated} links={index._links} me={me} />;
  }

  return (
    <div className="App">
      <Header>{navigation}</Header>
      {content}
      {isAuthenticated ? <Footer me={me} version={index.version} links={index._links} />: null}
    </div>
  );
};

export default App;
