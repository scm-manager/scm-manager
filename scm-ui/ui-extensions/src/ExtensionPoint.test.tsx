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
import ExtensionPoint from "./ExtensionPoint";
import { shallow, mount } from "enzyme";
// eslint-disable-next-line no-restricted-imports
import "@scm-manager/ui-tests/enzyme";
import binder from "./binder";

jest.mock("./binder");

const mockedBinder = binder as jest.Mocked<typeof binder>;

describe("ExtensionPoint test", () => {
  beforeEach(() => {
    mockedBinder.hasExtension.mockReset();
    mockedBinder.getExtension.mockReset();
    mockedBinder.getExtensions.mockReset();
  });

  it("should render nothing, if no extension was bound", () => {
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtensions.mockReturnValue([]);
    const rendered = shallow(<ExtensionPoint name="something.special" />);
    expect(rendered.text()).toBe("");
  });

  it("should render the given component", () => {
    const label = () => {
      return <label>Extension One</label>;
    };
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const rendered = mount(<ExtensionPoint name="something.special" />);
    expect(rendered.text()).toBe("Extension One");
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

    const rendered = mount(<ExtensionPoint name="something.special" renderAll={true} />);
    const text = rendered.text();
    expect(text).toContain("Extension One");
    expect(text).toContain("Extension Two");
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

    const rendered = mount(
      <ExtensionPoint
        name="something.special"
        props={{
          value: "Awesome",
        }}
      />
    );
    const text = rendered.text();
    expect(text).toContain("Awesome");
  });

  it("should render children, if no extension is bound", () => {
    const rendered = mount(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>
    );
    const text = rendered.text();
    expect(text).toContain("Cool stuff");
  });

  it("should not render children, if an extension was bound", () => {
    const label = () => {
      return <label>Bound Extension</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const rendered = mount(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>
    );
    const text = rendered.text();
    expect(text).toContain("Bound Extension");
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

    const rendered = mount(<App />);
    const text = rendered.text();
    expect(text).toBe("Hello Trillian");
  });

  it("should not render nothing without extension and without default", () => {
    mockedBinder.hasExtension.mockReturnValue(false);

    const rendered = mount(<ExtensionPoint name="something.special" />);
    const text = rendered.text();
    expect(text).toBe("");
  });

  it("should render an instance", () => {
    const Label = () => {
      return <label>Extension One</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(<Label />);

    const rendered = mount(<ExtensionPoint name="something.special" />);
    expect(rendered.text()).toBe("Extension One");
  });

  it("should render an instance with props", () => {
    const Label = ({ name }: { name: string }) => {
      return <label>Extension {name}</label>;
    };

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(<Label name="One" />);

    const rendered = mount(<ExtensionPoint name="something.special" props={{ name: "Two" }} />);
    expect(rendered.text()).toBe("Extension Two");
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

    const rendered = mount(<ExtensionPoint name="something.special" propTransformer={transformer} />);
    expect(rendered.text()).toBe("Extension Two");
  });

  it("should pass children as props", () => {
    const label: FC = ({ children }) => {
      return (
        <>
          <label>Bound Extension</label>
          <details>{children}</details>
        </>
      );
    };
    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtension.mockReturnValue(label);

    const rendered = mount(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>
    );
    const text = rendered.text();
    expect(text).toContain("Bound Extension");
    expect(text).toContain("Cool stuff");
  });

  it("should wrap children with multiple extensions", () => {
    const w1: FC = ({ children }) => (
      <>
        <label>Outer {"-> "}</label>
        <details>{children}</details>
      </>
    );

    const w2: FC = ({ children }) => (
      <>
        <label>Inner {"-> "}</label>
        <details>{children}</details>
      </>
    );

    mockedBinder.hasExtension.mockReturnValue(true);
    mockedBinder.getExtensions.mockReturnValue([w1, w2]);

    const rendered = mount(
      <ExtensionPoint name="something.special" renderAll={true} wrapper={true}>
        <p>Children</p>
      </ExtensionPoint>
    );
    const text = rendered.text();
    expect(text).toEqual("Outer -> Inner -> Children");;
  });
});
