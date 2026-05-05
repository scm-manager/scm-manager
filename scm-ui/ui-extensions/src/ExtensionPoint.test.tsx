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

import React, { FC, ReactNode } from "react";
import { Mocked } from "vitest";
import { render } from "@testing-library/react";
import ExtensionPoint from "./ExtensionPoint";
import binder from "./binder";

vi.mock("./binder");

const mockedBinder = binder as Mocked<typeof binder>;

describe("ExtensionPoint test", () => {
  beforeEach(() => {
    mockedBinder.hasExtension.mockReset();
    mockedBinder.getExtension.mockReset();
    mockedBinder.getExtensions.mockReset();
  });

  it("should render nothing, if no extension was bound", () => {
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtensions.mockReturnValue([]);
    const { container } = render(<ExtensionPoint name="something.special" />);
    assert.equal(container.textContent, "");
  });

  it("should render the given component", () => {
    const label = () => {
      return <label>Extension One</label>;
    };
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const { container } = render(<ExtensionPoint name="something.special" />);
    assert.include(container.textContent ?? "", "Extension One");
  });

  it("should render the given components", () => {
    const labelOne = () => {
      return <label>Extension One</label>;
    };
    const labelTwo = () => {
      return <label>Extension Two</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtensions.mockReturnValue([labelOne, labelTwo]);

    const { container } = render(<ExtensionPoint name="something.special" renderAll={true} />);
    assert.include(container.textContent ?? "", "Extension One");
    assert.include(container.textContent ?? "", "Extension Two");
  });

  it("should render the given component, with the given props", () => {
    type Props = {
      value: string;
    };

    const label = (props: Props) => {
      return <label>{props.value}</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const { container } = render(
      <ExtensionPoint
        name="something.special"
        props={{
          value: "Awesome",
        }}
      />,
    );
    assert.include(container.textContent ?? "", "Awesome");
  });

  it("should render children, if no extension is bound", () => {
    const { container } = render(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>,
    );
    assert.include(container.textContent ?? "", "Cool stuff");
  });

  it("should not render children, if an extension was bound", () => {
    const label = () => {
      return <label>Bound Extension</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const { container } = render(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>,
    );
    assert.include(container.textContent ?? "", "Bound Extension");
  });

  it("should pass the context of the parent component", () => {
    const UserContext = React.createContext({
      name: "anonymous",
    });

    type HelloProps = {
      name: string;
    };

    const Hello = (props: HelloProps) => {
      return <label>Hello {props.name}</label>;
    };

    const HelloUser = () => {
      return <UserContext.Consumer>{({ name }) => <Hello name={name} />}</UserContext.Consumer>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(HelloUser);

    const App = () => {
      return (
        <UserContext.Provider
          value={{
            name: "Trillian",
          }}
        >
          <ExtensionPoint name="hello" />
        </UserContext.Provider>
      );
    };

    const { container } = render(<App />);
    assert.include(container.textContent ?? "", "Hello Trillian");
  });

  it("should not render nothing without extension and without default", () => {
    mockedBinder.hasExtension.mockReturnValue(false);

    const { container } = render(<ExtensionPoint name="something.special" />);
    assert.include(container.textContent ?? "", "");
  });

  it("should render an instance", () => {
    const Label = () => {
      return <label>Extension One</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(<Label />);

    const { container } = render(<ExtensionPoint name="something.special" />);
    assert.include(container.textContent ?? "", "Extension One");
  });

  it("should render an instance with props", () => {
    const Label = ({ name }: { name: string }) => {
      return <label>Extension {name}</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(Label);

    const { container } = render(<ExtensionPoint name="something.special" props={{ name: "Two" }} />);
    assert.include(container.textContent ?? "", "Extension Two");
  });

  it("should transform extension, before render", () => {
    const label = ({ name = "One" }: { name: string }) => {
      return <label>Extension {name}</label>;
    };
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const transformer = (props: object) => {
      return {
        ...props,
        name: "Two",
      };
    };

    const transformedProps = transformer({});
    const { container } = render(<ExtensionPoint name="something.special" props={transformedProps} />);

    assert.include(container.textContent ?? "", "Extension Two");
  });

  it("should pass children as props", () => {
    const label: FC<{ children: ReactNode }> = ({ children }) => {
      return (
        <>
          <label>Bound Extension</label>
          <details>{children}</details>
        </>
      );
    };
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const { container } = render(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>,
    );
    assert.include(container.textContent ?? "", "Bound Extension");
    assert.include(container.textContent ?? "", "Cool stuff");
  });

  it("should wrap children with multiple extensions", () => {
    const w1: FC<{ children: ReactNode }> = ({ children }) => (
      <>
        <label>Outer {"-> "}</label>
        <details>{children}</details>
      </>
    );

    const w2: FC<{ children: ReactNode }> = ({ children }) => (
      <>
        <label>Inner {"-> "}</label>
        <details>{children}</details>
      </>
    );

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtensions.mockReturnValue([w1, w2]);

    const { container } = render(
      <ExtensionPoint name="something.special" renderAll={true} wrapper={true}>
        <p>Children</p>
      </ExtensionPoint>,
    );
    assert.include(container.textContent ?? "", "Outer -> Inner -> Children");
  });

  it("should render children of non fc", () => {
    const nonFc = (
      <div>
        <label>Non fc with children</label>
      </div>
    );

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(nonFc);

    const { container } = render(
      <ExtensionPoint name="something.special">
        <p>Children</p>
      </ExtensionPoint>,
    );
    assert.include(container.textContent ?? "", "Non fc with children");
  });
});
