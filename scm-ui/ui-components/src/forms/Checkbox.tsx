import React from 'react';
import { Help } from '../index';

type Props = {
  label?: string;
  name?: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled?: boolean;
  helpText?: string;
};

class Checkbox extends React.Component<Props> {
  onCheckboxChange = (event: SyntheticInputEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked, this.props.name);
    }
  };

  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return <Help message={helpText} />;
    }
  };

  render() {
    return (
      <div className="field is-grouped">
        <div className="control">
          <label className="checkbox" disabled={this.props.disabled}>
            <input
              type="checkbox"
              checked={this.props.checked}
              onChange={this.onCheckboxChange}
              disabled={this.props.disabled}
            />{' '}
            {this.props.label}
            {this.renderHelp()}
          </label>
        </div>
      </div>
    );
  }
}

export default Checkbox;
