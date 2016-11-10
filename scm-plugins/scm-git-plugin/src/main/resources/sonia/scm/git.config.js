/*
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

Ext.ns("Sonia.git");

Sonia.git.ConfigPanel = Ext.extend(Sonia.config.SimpleConfigForm, {

  // labels
  titleText: 'Git Settings',
  repositoryDirectoryText: 'Repository directory',
  gcExpressionText: 'Git GC Cron Expression',
  disabledText: 'Disabled',

  // helpTexts
  repositoryDirectoryHelpText: 'Location of the Git repositories.',
  // TODO i18n
  gcExpressionHelpText: '<p>Use Quartz Cron Expressions (SECOND MINUTE HOUR DAYOFMONTH MONTH DAYOFWEEK) to run git gc in intervals.</p>\n\
                         <table>\n\
                         <tr><th><b>SECOND</b></th><td>Seconds within the minute (0–59)</td></tr>\n\
                         <tr><th><b>MINUTE</b></th><td>Minutes within the hour (0–59)</td></tr>\n\
                         <tr><th><b>HOUR</b></th><td>The hour of the day (0–23)</td></tr>\n\
                         <tr><th><b>DAYOFMONTH</b></th><td>The day of the month (1–31)</td></tr>\n\
                         <tr><th><b>MONTH</b></th><td>The month (1–12)</td></tr>\n\
                         <tr><th><b>DAYOFWEEK</b></th><td>The day of the week (MON, TUE, WED, THU, FRI, SAT, SUN)</td></tr>\n\
                         </table>\n\
                         <p>E.g.: To run the task on every sunday at two o\'clock in the morning: 0 0 2 ? * SUN</p>\n\
                         <p>For more informations please have a look at <a href="http://www.quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/crontrigger.html">Quartz CronTrigger</a></p>',
  disabledHelpText: 'Enable or disable the Git plugin.\n\
                    Note you have to reload the page, after changing this value.',

  initComponent: function(){

    var config = {
      title : this.titleText,
      configUrl: restUrl + 'config/repositories/git.json',
      items : [{
        xtype: 'textfield',
        name: 'repositoryDirectory',
        fieldLabel: this.repositoryDirectoryText,
        helpText: this.repositoryDirectoryHelpText,
        allowBlank : false
      },{
        xtype: 'textfield',
        name: 'gc-expression',
        fieldLabel: this.gcExpressionText,
        helpText: this.gcExpressionHelpText,
        allowBlank : true        
      },{
        xtype: 'checkbox',
        name: 'disabled',
        fieldLabel: this.disabledText,
        inputValue: 'true',
        helpText: this.disabledHelpText
      }]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.git.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }

});

Ext.reg("gitConfigPanel", Sonia.git.ConfigPanel);

// add default branch chooser to settings panel
Sonia.git.GitSettingsFormPanel = Ext.extend(Sonia.repository.SettingsFormPanel, {
  
  defaultBranchText: 'Default Branch',
  defaultBranchHelpText: 'The default branch which is show first on source or commit view.',
  
  modifyDefaultConfig: function(config){
    if (this.item) {
      var position = -1;
      for ( var i=0; i<config.items.length; i++ ) {
        var field = config.items[i];
        if (field.name === 'public') {
          position = i;
          break;
        }
      }
      
      var defaultBranchComboxBox = {
        fieldLabel: this.defaultBranchText,
        name: 'defaultBranch',
        repositoryId: this.item.id,
        value: this.getDefaultBranch(this.item),
        useNameAsValue: true,
        xtype: 'repositoryBranchComboBox',
        helpText: this.defaultBranchHelpText
      };
      
      if (position >= 0) {
        config.items.splice(position, 0, defaultBranchComboxBox);
      } else {
        config.items.push(defaultBranchComboxBox);
      }
    }
  },
  
  getDefaultBranch: function(item){
    if (item.properties) {
      for ( var i=0; i<item.properties.length; i++ ) {
        var prop = item.properties[i];
        if (prop.key === 'git.default-branch') {
          return prop.value;
        }
      }
    }
    return undefined;
  },
  
  setDefaultBranch: function(item, defaultBranch){
    if (!item.properties) {
      item.properties = [{
          key: 'git.default-branch',
          value: defaultBranch
      }];
    } else {
      
      var found = false;
      for ( var i=0; i<item.properties.length; i++ ) {
        var prop = item.properties[i];
        if (prop.key === 'git.default-branch') {
          prop.value = defaultBranch;
          found = true;
          break;
        }
      }
      
      if (!found) {
        item.properties.push({
          key: 'git.default-branch',
          value: defaultBranch
        });
      }
    }
  },
  
  prepareUpdate: function(item) {
    if (item.defaultBranch) {
      var defaultBranch = item.defaultBranch;
      delete item.defaultBranch;
      this.setDefaultBranch(item, defaultBranch);
    }
  }
  
});

Ext.reg("gitSettingsForm", Sonia.git.GitSettingsFormPanel);


// i18n

if ( i18n && i18n.country === 'de' ){

  Ext.override(Sonia.git.ConfigPanel, {

    // labels
    titleText: 'Git Einstellungen',
    repositoryDirectoryText: 'Repository-Verzeichnis',
    disabledText: 'Deaktivieren',

    // helpTexts
    repositoryDirectoryHelpText: 'Verzeichnis der Git-Repositories.',
    disabledHelpText: 'Aktivieren oder deaktivieren des Git Plugins.\n\
      Die Seite muss neu geladen werden wenn dieser Wert geändert wird.'
    
  });
  
  Ext.override(Sonia.git.GitSettingsFormPanel, {
    
    // labels
    defaultBranchText: 'Standard Branch',
    
    // helpTexts
    defaultBranchHelpText: 'Der Standard Branch wird für die Source und Commit Ansicht verwendet, \n\
                            wenn kein anderer Branch eingestellt wurde.'
    
  });

}


// register information panel

initCallbacks.push(function(main){
  main.registerInfoPanel('git', {
    checkoutTemplate: 'git clone <a href="{0}" target="_blank">{0}</a>',
    xtype: 'repositoryExtendedInfoPanel'
  });
  main.registerSettingsForm('git', {
    xtype: 'gitSettingsForm'
  });
});

// register panel

registerConfigPanel({
  xtype : 'gitConfigPanel'
});

// register type icon

Sonia.repository.typeIcons['git'] = 'resources/images/icons/16x16/git.png';
