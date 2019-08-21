//@flow
import React from "react";
import type { Plugin } from "@scm-manager/ui-types";
import PluginGroupEntry from "../components/PluginGroupEntry";
import groupByCategory from "./groupByCategory";

type Props = {
  plugins: Plugin[]
};

class PluginList extends React.Component<Props> {
  render() {
    const { plugins } = this.props;

    const groups = groupByCategory(plugins);
    return (
      <div className="content is-plugin-page">
        {groups.map(group => {
          return <PluginGroupEntry group={group} key={group.name} />;
        })}
      </div>
    );
  }
}

export default PluginList;
