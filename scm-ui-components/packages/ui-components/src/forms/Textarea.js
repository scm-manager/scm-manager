//@flow
import React from "react";
import {Help} from "../index";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  label?: string,
  placeholder?: SelectItem[],
  value?: string,
  onChange: string => void,
  helpText?: string
};

class Textarea extends React.Component<Props> {
  field: ?HTMLTextAreaElement;

  handleInput = (event: SyntheticInputEvent<HTMLTextAreaElement>) => {
    this.props.onChange(event.target.value);
  };

  renderLabel = () => {
    const label = this.props.label;
    if (label) {
      return <label className="label">{label}</label>;
    }
    return "";
  };

  renderHelp = () => {
    const helpText = this.props.helpText;
    if(helpText){
      return (
        <div className="control columns is-vcentered">
          <Help message={helpText} />
        </div>);
    }
    else
      return null;
  };

  render() {
    const { placeholder, value } = this.props;

    return (
      <div className="field">
        <div className="field is-grouped">
          <div className="control">
            {this.renderLabel()}
          </div>
          {this.renderHelp()}
        </div>
        <div className="control">
          <textarea
            className="textarea"
            ref={input => {
              this.field = input;
            }}
            placeholder={placeholder}
            onChange={this.handleInput}
            value={value}
          />
        </div>
      </div>
    );
  }
}

export default Textarea;
