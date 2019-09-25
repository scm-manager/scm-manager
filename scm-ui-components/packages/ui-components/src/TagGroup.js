//@flow
import * as React from "react";
import injectSheet from "react-jss";
import { Help, Tag } from "./index";

type Props = {
  items: string[],
  label: string,
  helpText?: string,
  onRemove: (string[]) => void,

  // context props
  classes: Object
};

const styles = {
  help: {
    position: "relative"
  }
};

class TagGroup extends React.Component<Props> {
  render() {
    const { items, label, helpText, classes } = this.props;
    let help = null;
    if (helpText) {
      help = <Help className={classes.help} message={helpText} />;
    }
    return (
      <div className="field is-grouped is-grouped-multiline">
        {label && items ? (
          <div className="control">
            <strong>
              {label}
              {help}
              {items.length > 0 ? ":" : ""}
            </strong>
          </div>
        ) : (
          ""
        )}
        {items.map((item, key) => {
          return (
            <div className="control" key={key}>
              <div className="tags has-addons">
                <Tag
                  color="info is-outlined"
                  label={item}
                  onRemove={() => this.removeEntry(item)}
                />
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  removeEntry = (item: string) => {
    const newItems = this.props.items.filter(name => name !== item);
    this.props.onRemove(newItems);
  };
}

export default injectSheet(styles)(TagGroup);
