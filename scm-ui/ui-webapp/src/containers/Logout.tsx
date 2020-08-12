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
import React from "react";
import {connect} from "react-redux";
import {WithTranslation, withTranslation} from "react-i18next";

import {getLogoutFailure, logout} from "../modules/auth";
import {ErrorPage, Loading} from "@scm-manager/ui-components";
import {getLogoutLink} from "../modules/indexResource";
import {RouteComponentProps, withRouter} from "react-router-dom";
import {compose} from "redux";

type Props = RouteComponentProps &
  WithTranslation & {
  error: Error;
  logoutLink: string;

  // dispatcher functions
  logout: (link: string, callback: () => void) => void;
};

class Logout extends React.Component<Props> {
  componentDidMount() {
    if (this.props.logoutLink) {
      this.props.logout(this.props.logoutLink, () => this.props.history.push("/login"));
    }
  }

  render() {
    const {error, t} = this.props;
    if (error) {
      return <ErrorPage title={t("logout.error.title")} subtitle={t("logout.error.subtitle")} error={error}/>;
    } else {
      return <Loading/>;
    }
  }
}

const mapStateToProps = (state: any) => {
  const error = getLogoutFailure(state);
  const logoutLink = getLogoutLink(state);
  return {
    error,
    logoutLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    logout: (link: string, callback: () => void) => dispatch(logout(link, callback))
  };
};

export default compose(withTranslation("commons"), withRouter, connect(mapStateToProps, mapDispatchToProps))(Logout);
