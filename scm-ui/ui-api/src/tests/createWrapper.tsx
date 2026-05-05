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

import React, { FC, ReactNode, Suspense } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

class SimpleErrorBoundary extends React.Component<{ children: ReactNode }, { hasError: boolean }> {
  constructor(props: { children: ReactNode }) {
    super(props);
    this.state = { hasError: false };
  }
  static getDerivedStateFromError() {
    return { hasError: true };
  }

  render() {
    if (this.state.hasError) {
      return <h1>ERROR</h1>;
    }
    return this.props.children;
  }
}

const createWrapper = (queryClient?: QueryClient): FC<{ children: ReactNode }> => {
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient ? queryClient : new QueryClient()}>
      <SimpleErrorBoundary>
        <Suspense>{children}</Suspense>
      </SimpleErrorBoundary>
    </QueryClientProvider>
  );
};

export default createWrapper;
