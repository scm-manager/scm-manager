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
import React, { FC, ReactNode } from "react";
import Logo from "./../Logo";
import styled from "styled-components";

type Props = {
  authenticated: boolean;
  children?: ReactNode;
};

const SmallHeader = styled.div`
  padding-top: 1rem;
`;

const renderSmallHeader = (children: ReactNode) => {
  return (
    <section className="hero is-dark is-small">
      <SmallHeader className="hero-foot">
        <div className="container">{children}</div>
      </SmallHeader>
    </section>
  );
};

const renderLargeHeader = (children: ReactNode) => {
  return (
    <section className="hero is-dark is-small">
      <div className="hero-body">
        <div className="container">
          <div className="columns is-vcentered">
            <div className="column">
              <Logo />
            </div>
          </div>
        </div>
      </div>
      <div className="hero-foot">
        <div className="container">{children}</div>
      </div>
    </section>
  );
};

const Header: FC<Props> = ({ children, authenticated }) => {
  if (authenticated) {
    return renderSmallHeader(children);
  } else {
    return renderLargeHeader(children);
  }
};

export default Header;
