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
    pluginurlText: 'Dieses Feld sollte eine URL enthalten. Format: \n\
      "http://plugins.scm-manager.org/scm-plugin-backend/api/{version}/plugins?os={os}&arch={arch}&snapshot=false"',

    // sonia.user.js
    passwordText: 'Die Passwörter stimmen nicht überein!'
  });

}

// sonia.login.js

if (Sonia.login.Form){

  Ext.override(Sonia.login.Form, {
    usernameText: 'Benutzername',
    passwordText: 'Passwort',
    loginText: 'Anmelden',
    cancelText: 'Abbrechen',
    titleText: 'Anmeldung',
    waitTitleText: 'Verbinden',
    WaitMsgText: 'Übertrage Daten...',
    failedMsgText: 'Anmeldung fehlgeschlagen!'
  });

}

// sonia.rest.js

if ( Sonia.rest.JsonStore ){

  Ext.override(Sonia.rest.JsonStore, {
    errorTitleText: 'Fehler',
    errorMsgText: 'Es konnten keine Daten geladen werden. Server-Status: {0}'
  });

}

if ( Sonia.rest.Grid ){

  Ext.override(Sonia.rest.Grid, {
    emptyText: 'Es konnten keine Daten gefunden werden.'
  });

}

if ( Sonia.rest.FormPanel ){

  Ext.override(Sonia.rest.FormPanel, {
    okText: 'Ok',
    cancelText: 'Abbrechen',
    addText: 'Hinzufügen',
    removeText: 'Entfernen'
  });

}

// sonia.repository.js

if ( Sonia.repository.Grid ){

  Ext.override(Sonia.repository.Grid, {
    colNameText: 'Name',
    colTypeText: 'Typ',
    colContactText: 'Kontakt',
    colDescriptionText: 'Beschreibung',
    colCreationDateText: 'Erstellungsdatum',
    colUrlText: 'Url',
    emptyText: 'Es wurde kein Repository konfiguriert',
    formTitleText: 'Repository'
  });

}

if (Sonia.repository.FormPanel){

  Ext.override(Sonia.repository.FormPanel, {
    colGroupPermissionText: 'Grouppen Berechtigung',
    colNameText: 'Name',
    colTypeText: 'Typ',
    formTitleText: 'Einstellungen',
    nameText: 'Name',
    typeText: 'Typ',
    contactText: 'Kontakt',
    descriptionText: 'Beschreibung',
    publicText: 'Öffentlich',
    permissionText: 'Berechtigung',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Repository update fehlgeschlagen',
    createErrorMsgText: 'Repository erstellen fehlgeschlagen',

    // help TODO
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
    titleText: 'Repository',
    addText: 'Hinzufügen',
    removeText: 'Entfernen',
    reloadText: 'Aktualisieren',
    emptyText: 'Es wurde kein Repository selektiert',
    removeTitleText: 'Repository entfernen',
    removeMsgText: 'Repository entfernen "{0}"?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Repository entfernen fehlgeschlagen'
  });

}

// sonia.config.js

if (Sonia.config.ScmConfigPanel){

  Ext.override(Sonia.config.ScmConfigPanel,{
    titleText: 'Allgemeine Einstellung',
    servnameText: 'Servername',
    enableForwardingText: 'Forwarding (mod_proxy) aktivieren',
    forwardPortText: 'Forward Port',
    pluginRepositoryText: 'Plugin-Repository',
    allowAnonymousAccessText: 'Anonymen Zugriff erlauben',
    enableSSLText: 'SSL Aktivieren',
    sslPortText: 'SSL Port',
    adminGroupsText: 'Admin Gruppen',
    adminUsersText: 'Admin Benutzer',
    submitText: 'Senden ...',
    loadingText: 'Laden ...',
    errorTitleText: 'Fehler',
    errorMsgText: 'Die Konfiguration konnte nicht geladen werden.',

    // help TODO
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
    title: 'Konfiguration',
    saveButtonText: 'Speichern',
    resetButtontext: 'Reset',

    submitText: 'Senden ...',
    loadingText: 'Laden ...'
  });

}

// sonia.user.js

if (Sonia.user.Grid){

  Ext.override(Sonia.user.Grid, {
    titleText: 'Benutzer',
    colNameText: 'Name',
    colDisplayNameText: 'Anzeigename',
    colMailText: 'E-Mail',
    colAdminText: 'Admin',
    colCreationDateText: 'Erstellungsdatum',
    // TODO
    colLastModifiedText: 'Last modified',
    colTypeText: 'Typ'
  });

}

if (Sonia.user.FormPanel){

  Ext.override(Sonia.user.FormPanel, {
    nameText: 'Name',
    displayNameText: 'Anzeigename',
    mailText: 'E-Mail',
    passwordText: 'Passwort',
    adminText: 'Administrator',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Benutzer update fehlgeschlagen',
    createErrorMsgText: 'Benutzer erstellen fehlgeschlagen',
    passwordMinLengthText: 'Das Passwort muss mindestens 6 Zeichen lang sein',

    // help TODO
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
    addText: 'Hinzufügen',
    removeText: 'Entfernen',
    reloadText: 'Aktualisieren',
    titleText: 'Benutzer',
    emptyText: 'Es wurde kein Benutzer selektiert',
    removeTitleText: 'Benutzer entfernen',
    removeMsgText: 'Benutzer "{0}" entfernen?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Benutzer entfernen fehlgeschlagen'
  });

}


// sonia.group.js

if (Sonia.group.Grid){

  Ext.override(Sonia.group.Grid,{
    colNameText: 'Name',
    colDescriptionText: 'Beschreibung',
    colMembersText: 'Mitglieder',
    colCreationDateText: 'Erstellungsdatum',
    colTypeText: 'Typ',
    emptyGroupStoreText: 'Es wurde keine Gruppe konfiguriert',
    groupFormTitleText: 'Grouppe'
  });

}

if (Sonia.group.FormPanel){

  Ext.override(Sonia.group.FormPanel, {
    colMemberText: 'Mitglied',
    titleText: 'Einstellungen',
    nameText: 'Name',
    descriptionText: 'Beschreibung',
    membersText: 'Mitglieder',
    errorTitleText: 'Fehler',
    updateErrorMsgText: 'Gruppen update fehlgeschlagen',
    createErrorMsgText: 'Gruppen erstellen fehlgeschlagen',

    // help TODO
    nameHelpText: 'The unique name of the group.',
    descriptionHelpText: 'A short description of the group.',
    membersHelpText: 'The usernames of the group members.'
  });

}

if (Sonia.group.Panel){

  Ext.override(Sonia.group.Panel, {
    addText: 'Hinzufügen',
    removeText: 'Entfernen',
    reloadText: 'Aktualisieren',
    titleText: 'Gruppe',
    emptyText: 'Es wurde keine Gruppe selektiert',
    removeTitleText: 'Gruppe entfernen',
    removeMsgText: 'Gruppe "{0}" entfernen?',
    errorTitleText: 'Fehler',
    errorMsgText: 'Gruppe entfernen fehlgeschlagen'
  });

}

// sonia.action.js

if (Sonia.action.ChangePasswordWindow){

  Ext.override(Sonia.action.ChangePasswordWindow, {
    titleText: 'Passwort ändern',
    oldPasswordText: 'Altes Passwort',
    newPasswordText: 'Neues Password',
    confirmPasswordText: 'Passwort bestätigen',
    okText: 'Ok',
    cancelText: 'Abbrechen',
    connectingText: 'Verbinden',
    failedText: 'Passwort ändern fehlgeschlagen!',
    waitMsgText: 'Daten übertragen...'
  });

}

// sonia.plugin.js

if (Sonia.plugin.Center){

  Ext.override(Sonia.plugin.Center, {
    waitTitleText: 'Bitte warten',
    errorTitleText: 'Fehler',
    restartText: 'Der ApplicationServer muss neugestartet werden um das Plugin zu aktivieren.',

    installWaitMsgText: 'Plugin wird installiert.',
    installSuccessText: 'Plugin wurde erfolgreich installiert',
    installFailedText: 'Plugin installieren fehlgeschlagen',

    uninstallWaitMsgText: 'Plugin wird deinstalliert.',
    uninstallSuccessText: 'Plugin wurde erfolgreich deinstalliert',
    uninstallFailedText: 'Plugin deinstallieren fehlgeschlagen',

    updateWaitMsgText: 'Plugin wird aktualisiert.',
    updateSuccessText: 'Plugin wurde erfolgreich aktualisiert',
    updateFailedText: 'Plugin aktualisieren fehlgeschlagen'
  });

}

if (Sonia.plugin.Grid){

  Ext.override(Sonia.plugin.Grid, {
    colNameText: 'Name',
    colAuthorText: 'Autor',
    colDescriptionText: 'Beschreibung',
    colVersionText: 'Version',
    colActionText: 'Aktion',
    colUrlText: 'Url',
    emptyText: 'Es konnte kein Plugin gefunden werden'
  });

}

// sonia.scm.js

if (Sonia.scm.Main){

  Ext.override(Sonia.scm.Main, {
    tabRepositoriesText: 'Repositories',
    navChangePasswordText: 'Passwort ändern',
    sectionMainText: 'Repositories',
    sectionSecurityText: 'Sicherheit',
    navRepositoriesText: 'Repositories',
    sectionConfigText: 'Konfiguration',
    navGeneralConfigText: 'Allgemein',
    tabGeneralConfigText: 'SCM Konfiguration',

    navRepositoryTypesText: 'Repository Konfiguration',
    tabRepositoryTypesText: 'Repository Konfiguration',
    navPluginsText: 'Plugins',
    tabPluginsText: 'Plugins',
    navUsersText: 'Benutzer',
    tabUsersText: 'Benutzer',
    navGroupsText: 'Gruppen',
    tabGroupsText: 'Gruppen',

    sectionLoginText: 'Anmelden',
    navLoginText: 'Anmelden',

    sectionLogoutText: 'Abmelden',
    navLogoutText: 'Abmelden',

    logoutFailedText: 'Abmelden Fehlgeschlagen!'
  });

}