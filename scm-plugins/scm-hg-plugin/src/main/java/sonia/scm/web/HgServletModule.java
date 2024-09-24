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

package sonia.scm.web;


import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.api.v2.resources.HgGlobalConfigDtoToHgConfigMapper;
import sonia.scm.api.v2.resources.HgGlobalConfigToHgGlobalConfigDtoMapper;
import sonia.scm.api.v2.resources.HgRepositoryConfigMapper;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.spi.*;


@Extension
public class HgServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(HgGlobalConfigDtoToHgConfigMapper.class).to(Mappers.getMapperClass(HgGlobalConfigDtoToHgConfigMapper.class));
    bind(HgGlobalConfigToHgGlobalConfigDtoMapper.class).to(Mappers.getMapperClass(HgGlobalConfigToHgGlobalConfigDtoMapper.class));
    bind(HgRepositoryConfigMapper.class).to(Mappers.getMapperClass(HgRepositoryConfigMapper.class));

    bind(HgWorkingCopyFactory.class).to(SimpleHgWorkingCopyFactory.class);

    install(new FactoryModuleBuilder().implement(BlameCommand.class, HgBlameCommand.class).build(HgBlameCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BranchesCommand.class, HgBranchesCommand.class).build(HgBranchesCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BranchCommand.class, HgBranchCommand.class).build(HgBranchCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BrowseCommand.class, HgBrowseCommand.class).build(HgBrowseCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(CatCommand.class, HgCatCommand.class).build(HgCatCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(DiffCommand.class, HgDiffCommand.class).build(HgDiffCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(IncomingCommand.class, HgIncomingCommand.class).build(HgIncomingCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(LogCommand.class, HgLogCommand.class).build(HgLogCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ModificationsCommand.class, HgModificationsCommand.class).build(HgModificationsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(OutgoingCommand.class, HgOutgoingCommand.class).build(HgOutgoingCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(PullCommand.class, HgPullCommand.class).build(HgPullCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(HgLazyChangesetResolver.class, HgLazyChangesetResolver.class).build(HgLazyChangesetResolver.Factory.class));
    install(new FactoryModuleBuilder().implement(HgRepositoryHookEventFactory.class, HgRepositoryHookEventFactory.class).build(HgRepositoryHookEventFactory.Factory.class));
    install(new FactoryModuleBuilder().implement(PushCommand.class, HgPushCommand.class).build(HgPushCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ModifyCommand.class, HgModifyCommand.class).build(HgModifyCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(TagsCommand.class, HgTagsCommand.class).build(HgTagsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(TagCommand.class, HgTagCommand.class).build(HgTagCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BundleCommand.class, HgBundleCommand.class).build(HgBundleCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(UnbundleCommand.class, HgUnbundleCommand.class).build(HgUnbundleCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(FullHealthCheckCommand.class, HgFullHealthCheckCommand.class).build(HgFullHealthCheckCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(BranchDetailsCommand.class, HgBranchDetailsCommand.class).build(HgBranchDetailsCommand.Factory.class));
    install(new FactoryModuleBuilder().implement(ChangesetsCommand.class, HgChangesetsCommand.class).build(HgChangesetsCommand.Factory.class));
  }
}
