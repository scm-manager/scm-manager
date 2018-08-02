//@flow
import React from "react";

export type SelectItem = {
  value: string,
  label: string
};

type Props = {
  label?: string,
  placeholder?: SelectItem[],
  value?: string,
  onChange: string => void
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

  render() {
    const { placeholder, value } = this.props;

    return (
      <div className="field">
        {this.renderLabel()}
        <div className="control">
          <textarea
            className="textarea"
            ref={input => {
              this.field = input;
            }}
            placeholder={placeholder}
            onChange={this.handleInput}
          >
            {value}
          </textarea>
        </div>
      </div>
    );
  }
}

export default Textarea;
