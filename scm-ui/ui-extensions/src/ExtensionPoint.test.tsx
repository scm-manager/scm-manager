import React from 'react';
import ExtensionPoint from './ExtensionPoint';
import { shallow, mount } from 'enzyme';
import '@scm-manager/ui-tests/enzyme';
import binder from './binder';

jest.mock('./binder');

describe('ExtensionPoint test', () => {
  beforeEach(() => {
    binder.hasExtension.mockReset();
    binder.getExtension.mockReset();
    binder.getExtensions.mockReset();
  });

  it('should render nothing, if no extension was bound', () => {
    binder.hasExtension.mockReturnValue(true);
    binder.getExtensions.mockReturnValue([]);
    const rendered = shallow(<ExtensionPoint name="something.special" />);
    expect(rendered.text()).toBe('');
  });

  it('should render the given component', () => {
    const label = () => {
      return <label>Extension One</label>;
    };
    binder.hasExtension.mockReturnValue(true);
    binder.getExtension.mockReturnValue(label);

    const rendered = mount(<ExtensionPoint name="something.special" />);
    expect(rendered.text()).toBe('Extension One');
  });

  // We use this wrapper since Enzyme cannot handle React Fragments (see https://github.com/airbnb/enzyme/issues/1213)
  class ExtensionPointEnzymeFix extends ExtensionPoint {
    render() {
      return <div>{super.render()}</div>;
    }
  }
  it('should render the given components', () => {
    const labelOne = () => {
      return <label>Extension One</label>;
    };
    const labelTwo = () => {
      return <label>Extension Two</label>;
    };

    binder.hasExtension.mockReturnValue(true);
    binder.getExtensions.mockReturnValue([labelOne, labelTwo]);

    const rendered = mount(
      <ExtensionPointEnzymeFix name="something.special" renderAll={true} />,
    );
    const text = rendered.text();
    expect(text).toContain('Extension One');
    expect(text).toContain('Extension Two');
  });

  it('should render the given component, with the given props', () => {
    type Props = {
      value: string;
    };

    const label = (props: Props) => {
      return <label>{props.value}</label>;
    };

    binder.hasExtension.mockReturnValue(true);
    binder.getExtension.mockReturnValue(label);

    const rendered = mount(
      <ExtensionPoint
        name="something.special"
        props={{
          value: 'Awesome',
        }}
      />,
    );
    const text = rendered.text();
    expect(text).toContain('Awesome');
  });

  it('should render children, if no extension is bound', () => {
    const rendered = mount(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>,
    );
    const text = rendered.text();
    expect(text).toContain('Cool stuff');
  });

  it('should not render children, if an extension was bound', () => {
    const label = () => {
      return <label>Bound Extension</label>;
    };

    binder.hasExtension.mockReturnValue(true);
    binder.getExtension.mockReturnValue(label);

    const rendered = mount(
      <ExtensionPoint name="something.special">
        <p>Cool stuff</p>
      </ExtensionPoint>,
    );
    const text = rendered.text();
    expect(text).toContain('Bound Extension');
  });

  it('should pass the context of the parent component', () => {
    const UserContext = React.createContext({
      name: 'anonymous',
    });

    type HelloProps = {
      name: string;
    };

    const Hello = (props: HelloProps) => {
      return <label>Hello {props.name}</label>;
    };

    const HelloUser = () => {
      return (
        <UserContext.Consumer>
          {({ name }) => <Hello name={name} />}
        </UserContext.Consumer>
      );
    };

    binder.hasExtension.mockReturnValue(true);
    binder.getExtension.mockReturnValue(HelloUser);

    const App = () => {
      return (
        <UserContext.Provider
          value={{
            name: 'Trillian',
          }}
        >
          <ExtensionPoint name="hello" />
        </UserContext.Provider>
      );
    };

    const rendered = mount(<App />);
    const text = rendered.text();
    expect(text).toBe('Hello Trillian');
  });
});
