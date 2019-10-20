import React from "react";
import {
  RemoveEntryOfTableButton,
  LabelWithHelpIcon
} from "@scm-manager/ui-components";

type Props = {
  items: string[];
  label: string;
  removeLabel: string;
  onRemove: (p1: string[], p2: string) => void;
  disabled: boolean;
  helpText: string;
};

class ArrayConfigTable extends React.Component<Props> {
  render() {
    const { label, disabled, removeLabel, items, helpText } = this.props;
    return (
      <div>
        <LabelWithHelpIcon label={label} helpText={helpText} />
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {items.map(item => {
              return (
                <tr key={item}>
                  <td>{item}</td>
                  <td>
                    <RemoveEntryOfTableButton
                      entryname={item}
                      removeEntry={this.removeEntry}
                      disabled={disabled}
                      label={removeLabel}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeEntry = (item: string) => {
    const newItems = this.props.items.filter(name => name !== item);
    this.props.onRemove(newItems, item);
  };
}

export default ArrayConfigTable;
