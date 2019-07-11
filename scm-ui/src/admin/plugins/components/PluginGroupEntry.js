//@flow
import React from "react";
import { CardColumnGroup } from "@scm-manager/ui-components";
import type { PluginGroup } from "@scm-manager/ui-types";
import PluginEntry from "./PluginEntry";

type Props = {
  group: PluginGroup
};

class PluginGroupEntry extends React.Component<Props> {
  render() {
    const { group } = this.props;
    const entries = group.plugins.map((plugin, index) => {
      return <PluginEntry plugin={plugin} key={index} />;
    });
    return <CardColumnGroup name={group.name} elements={entries} />;
  }
}

export default PluginGroupEntry;
