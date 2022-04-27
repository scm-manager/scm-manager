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
import InfoBox from "./InfoBox";
import { LoginInfo as LoginInfoResponse } from "@scm-manager/ui-types";
import LoginForm from "./LoginForm";
import { Loading } from "@scm-manager/ui-components";

type Props = {
  loginInfoLink?: string;
  loading?: boolean;
  error?: Error | null;
  loginHandler: (username: string, password: string) => void;
};

type State = {
  info?: LoginInfoResponse;
  loading?: boolean;
};

// eslint-disable-next-line @typescript-eslint/ban-types
type NoOpErrorBoundaryProps = {};

type NoOpErrorBoundaryState = {
  error?: Error;
};

class NoOpErrorBoundary extends React.Component<NoOpErrorBoundaryProps, NoOpErrorBoundaryState> {
  constructor(props: NoOpErrorBoundaryProps) {
    super(props);
    this.state = {};
  }

  static getDerivedStateFromError(error: Error) {
    return { error };
  }

  componentDidCatch(error: Error) {
    // eslint-disable-next-line no-console
    if (console && console.error) {
      // eslint-disable-next-line no-console
      console.error("failed to render login info", error);
    }
  }

  render() {
    if (this.state.error) {
      return null;
    }

    return this.props.children;
  }
}

class LoginInfo extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: !!props.loginInfoLink
    };
  }

  fetchLoginInfo = (url: string) => {
    return fetch(url)
      .then(response => response.json())
      .then(info => {
        this.setState({
          info,
          loading: false
        });
      });
  };

  timeout = (ms: number, promise: Promise<void>) => {
    return new Promise<void>((resolve, reject) => {
      setTimeout(() => {
        reject(new Error("timeout during fetch of login info"));
      }, ms);
      promise.then(resolve, reject);
    });
  };

  componentDidMount() {
    const { loginInfoLink } = this.props;
    if (!loginInfoLink) {
      return;
    }
    this.timeout(1000, this.fetchLoginInfo(loginInfoLink)).catch(() => {
      this.setState({
        loading: false
      });
    });
  }

  createInfoPanel = (info: LoginInfoResponse) => (
    <NoOpErrorBoundary>
      <div className="column is-7 is-offset-1 p-0">
        <InfoBox item={info.feature} type="feature" />
        <InfoBox item={info.plugin} type="plugin" />
      </div>
    </NoOpErrorBoundary>
  );

  render() {
    const { info, loading } = this.state;
    if (loading) {
      return <Loading />;
    }

    let infoPanel;
    if (info) {
      infoPanel = this.createInfoPanel(info);
    }

    return (
      <>
        <LoginForm {...this.props} />
        {infoPanel}
      </>
    );
  }
}

export default LoginInfo;
