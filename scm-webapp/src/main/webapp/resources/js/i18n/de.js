/* *
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */


if (Ext.form.VTypes){

  Ext.override(Ext.form.VTypes, {

    // sonia.config.js
    pluginurlText: 'This field should be a URL in the format \n\
      "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false"',

    // sonia.user.js
    passwordText: 'The passwords entered do not match!'
  });

}

// sonia.login.js

if (Sonia.login.Form){

  Ext.override(Sonia.login.Form, {
    usernameText: 'Username',
    passwordText: 'Password',
    loginText: 'Login',
    cancelText: 'Cancel',
    titleText: 'Please Login',
    waitTitleText: 'Connecting',
    WaitMsgText: 'Sending data...',
    failedMsgText: 'Login failed!'
  });

}

// sonia.rest.js

if ( Sonia.rest.JsonStore ){

  Ext.override(Sonia.rest.JsonStore, {
    errorTitleText: 'Error',
    errorMsgText: 'Could not load items. Server returned status: {0}'
  });

}

if ( Sonia.rest.Grid ){

  Ext.override(Sonia.rest.Grid, {
    emptyText: 'No items available'
  });

}

if ( Sonia.rest.FormPanel ){

  Ext.override(Sonia.rest.FormPanel, {
    okText: 'Ok',
    cancelText: 'Cancel',
    addText: 'Add',
    removeText: 'Remove'
  });

}

// sonia.repository.js

if ( Sonia.repository.Grid ){

  Ext.override(Sonia.repository.Grid, {
    colNameText: 'Name',
    colTypeText: 'Type',
    colContactText: 'Contact',
    colDescriptionText: 'Description',
    colCreationDateText: 'Creation date',
    colUrlText: 'Url',
    emptyText: 'No repository is configured',
    formTitleText: 'Repository Form'
  });

}

if (Sonia.repository.FormPanel){

  Ext.override(Sonia.repository.FormPanel, {
    colGroupPermissionText: 'Group Permission',
    colNameText: 'Name',
    colTypeText: 'Type',
    formTitleText: 'Settings',
    nameText: 'Name',
    typeText: 'Type',
    contactText: 'Contact',
    descriptionText: 'Description',
    publicText: 'Public',
    permissionText: 'Permission',
    errorTitleText: 'Error',
    updateErrorMsgText: 'Repository update failed',
    createErrorMsgText: 'Repository creation failed',

    // help
    nameHelpText: 'The name of the repository. This name would be part of the repository url.',
    typeHelpText: 'The type of the repository (e.g. Mercurial, Git or Subversion).',
    contactHelpText: 'An email address of the person who is in charge for this repository.',
    descriptionHelpText: 'A short description of the repository.',
    publicHelpText: 'A public repository which is readable by every person.',
    permissionHelpText: 'If the "Group Permission" box is checked, then the name represents the groupname otherwise the username.<br />\n\
      Type explenation:<br /><b>READ</b> = read permission<br /><b>WRITE</b> = read and write permission<br />\n\
      <b>OWNER</b> = read, write permissions and also the ability to manage the properties and permissions'
  });

}

if (Sonia.repository.Panel){

  Ext.override(Sonia.repository.Panel, {
    titleText: 'Repository Form',
    addText: 'Add',
    removeText: 'Remove',
    reloadText: 'Reload',
    emptyText: 'Add or select an Repository',
    removeTitleText: 'Remove Repository',
    removeMsgText: 'Remove Repository "{0}"?',
    errorTitleText: 'Error',
    errorMsgText: 'Repository deletion failed'
  });

}

// sonia.config.js

if (Sonia.config.ScmConfigPanel){

  Ext.override(Sonia.config.ScmConfigPanel,{
    titleText: 'General Settings',
    servnameText: 'Servername',
    enableForwardingText: 'Enable forwarding (mod_proxy)',
    forwardPortText: 'Forward Port',
    pluginRepositoryText: 'Plugin repository',
    allowAnonymousAccessText: 'Allow Anonymous Access',
    enableSSLText: 'Enable SSL',
    sslPortText: 'SSL Port',
    adminGroupsText: 'Admin Groups',
    adminUsersText: 'Admin Users',
    submitText: 'Submit ...',
    loadingText: 'Loading ...',
    errorTitleText: 'Error',
    errorMsgText: 'Could not load config.',

    // help
    servernameHelpText: 'The name of this server. This name would be part of the repository url.',
    pluginRepositoryHelpText: 'The url of the plugin repository.<br />Explanation of the {placeholders}:\n\
      <br /><b>version</b> = SCM-Manager Version<br /><b>os</b> = Operation System<br /><b>arch</b> = Architecture',
    enableForwardingHelpText: 'Enbale mod_proxy port forwarding.',
    forwardPortHelpText: 'The forwarding port.',
    allowAnonymousAccessHelpText: 'Anonymous users can see public repositories.',
    enableSSLHelpText: 'Enable SSL.',
    sslPortHelpText: 'The ssl port.',
    adminGroupsHelpText: 'Comma seperated list of groups with admin rights.',
    adminUsersHelpText: 'Comma seperated list of users with admin rights.'
  });

}

if (Sonia.config.ConfigForm){

  Ext.override(Sonia.config.ConfigForm, {
    title: 'Config Form',
    saveButtonText: 'Save',
    resetButtontext: 'Reset',

    submitText: 'Submit ...',
    loadingText: 'Loading ...'
  });

}

// sonia.user.js

if (Sonia.user.Grid){

  Ext.override(Sonia.user.Grid, {
    titleText: 'User Form',
    colNameText: 'Name',
    colDisplayNameText: 'Display Name',
    colMailText: 'Mail',
    colAdminText: 'Admin',
    colCreationDateText: 'Creation Date',
    colLastModifiedText: 'Last modified',
    colTypeText: 'Type'
  });

}

if (Sonia.user.FormPanel){

  Ext.override(Sonia.user.FormPanel, {
    nameText: 'Name',
    displayNameText: 'Display name',
    mailText: 'Mail',
    passwordText: 'Password',
    adminText: 'Administrator',
    errorTitleText: 'Error',
    updateErrorMsgText: 'User update failed',
    createErrorMsgText: 'User creation failed',
    passwordMinLengthText: 'Password must be at least 6 characters long',

    // help
    usernameHelpText: 'The unique name of the user.',
    displayNameHelpText: 'The display name of the user.',
    mailHelpText: 'The email address of the user.',
    passwordHelpText: 'The plain text password of the user.',
    passwordConfirmHelpText: 'Repeat the password for validation.',
    adminHelpText: 'An administrator is able to create, modify and delete repositories, groups and users.'
  });

}

if (Sonia.user.Panel){

  Ext.override(Sonia.user.Panel, {
    addText: 'Add',
    removeText: 'Remove',
    reloadText: 'Reload',
    titleText: 'User Form',
    emptyText: 'Add or select an User',
    removeTitleText: 'Remove User',
    removeMsgText: 'Remove User "{0}"?',
    errorTitleText: 'Error',
    errorMsgText: 'User deletion failed'
  });

}


// sonia.group.js

if (Sonia.group.Grid){

  Ext.override(Sonia.group.Grid,{
    colNameText: 'Name',
    colDescriptionText: 'Description',
    colMembersText: 'Members',
    colCreationDateText: 'Creation date',
    colTypeText: 'Type',
    emptyGroupStoreText: 'No group is configured',
    groupFormTitleText: 'Group Form'
  });

}

if (Sonia.group.FormPanel){

  Ext.override(Sonia.group.FormPanel, {
    colMemberText: 'Member',
    titleText: 'Settings',
    nameText: 'Name',
    descriptionText: 'Description',
    membersText: 'Members',
    errorTitleText: 'Error',
    updateErrorMsgText: 'Group update failed',
    createErrorMsgText: 'Group creation failed',

    // help
    nameHelpText: 'The unique name of the group.',
    descriptionHelpText: 'A short description of the group.',
    membersHelpText: 'The usernames of the group members.'
  });

}

if (Sonia.group.Panel){

  Ext.override(Sonia.group.Panel, {
    addText: 'Add',
    removeText: 'Remove',
    reloadText: 'Reload',
    titleText: 'Group Form',
    emptyText: 'Add or select a Group',
    removeTitleText: 'Remove Group',
    removeMsgText: 'Remove Group "{0}"?',
    errorTitleText: 'Error',
    errorMsgText: 'Group deletion failed'
  });

}

// sonia.action.js

if (Sonia.action.ChangePasswordWindow){

  Ext.override(Sonia.action.ChangePasswordWindow, {
    titleText: 'Change Password',
    oldPasswordText: 'Old Password',
    newPasswordText: 'New Password',
    confirmPasswordText: 'Confirm Password',
    okText: 'Ok',
    cancelText: 'Cancel',
    connectingText: 'Connecting',
    failedText: 'change password failed!',
    waitMsgText: 'Sending data...'
  });

}

// sonia.plugin.js

if (Sonia.plugin.Center){

  Ext.override(Sonia.plugin.Center, {
    waitTitleText: 'Please wait',
    errorTitleText: 'Error',
    restartText: 'Restart the applicationserver to activate the plugin.',

    installWaitMsgText: 'Installing Plugin.',
    installSuccessText: 'Plugin successfully installed',
    installFailedText: 'Plugin installation failed',

    uninstallWaitMsgText: 'Uninstalling Plugin.',
    uninstallSuccessText: 'Plugin successfully uninstalled',
    uninstallFailedText: 'Plugin uninstallation failed',

    updateWaitMsgText: 'Updating Plugin.',
    updateSuccessText: 'Plugin successfully updated',
    updateFailedText: 'Plugin update failed'
  });

}

if (Sonia.plugin.Grid){

  Ext.override(Sonia.plugin.Grid, {
    colNameText: 'Name',
    colAuthorText: 'Author',
    colDescriptionText: 'Description',
    colVersionText: 'Version',
    colActionText: 'Action',
    colUrlText: 'Url',
    emptyText: 'No plugins avaiable'
  });

}

// sonia.scm.js

if (Sonia.scm.Main){

  Ext.override(Sonia.scm.Main, {
    tabRepositoriesText: 'Repositories',
    navChangePasswordText: 'Change Password',
    sectionMainText: 'Main',
    sectionSecurityText: 'Security',
    navRepositoriesText: 'Repositories',
    sectionConfigText: 'Config',
    navGeneralConfigText: 'General',
    tabGeneralConfigText: 'SCM Config',

    navRepositoryTypesText: 'Repository Types',
    tabRepositoryTypesText: 'Repository Config',
    navPluginsText: 'Plugins',
    tabPluginsText: 'Plugins',
    navUsersText: 'Users',
    tabUsersText: 'Users',
    navGroupsText: 'Groups',
    tabGroupsText: 'Groups',

    sectionLoginText: 'Login',
    navLoginText: 'Login',

    sectionLogoutText: 'Log out',
    navLogoutText: 'Log out',

    logoutFailedText: 'Logout Failed!'
  });

}