# -*- mode: ruby -*-
# vi: set ft=ruby :

# WHY?: The SCM-Manager Maven 'release' profile fails on Windows host due to missing character escaping in plugin i.e.
#    [ERROR] Failed to execute goal sonia.maven:web-compressor:1.5:partial-compress-template (default) \
#            on project scm-webapp: Execution default of goal sonia.maven:web-compressor:1.5:partial-compress-template \
#            failed: character to be escaped is missingBuilding on Windows

# Create Linux build environment
# Install Vagrant and VirtualBox on a Windows host
#   choco install VirtualBox
#   choco install vagrant
# Install vagrant plugin to install VirtualBox Guest Additions and allow mounted folders
#   vagrant plugin install vagrant-vbguest
# Start the centos VM (This can take some time):
#   vagrant up
# Log into the vagrant box:
#   vagrant ssh
#   $ cd /vagrant
# Build scm-manager and install artifacts to local maven cache
# The 'release' profile also compresses the javascript resources
#   $ mvn clean install -P release -Dmaven.test.skip=true -DskipTests
# Now start the Jetty instance to test it on http://localhost:8080
#   $ mvn jetty:run-war -Dmaven.test.skip=true -DskipTests -f scm-webapp.pom.xml


# If regularaly re-building the VM, the vagrant up step can be slow
# Package the pre-built VM and use that as the starting point for your builds.
#    vagrant package --output target/scm-manager-builder.box
# Add this box to the vagrant box cache
#    vagrant box add --name scm-manager-builder target/scm-manager-builder.box
# Destroy the current instance of the box
#    vagrant destroy
# Start a new instance with the pre-provisioned box
# NOTE: Only need to declare the VAGRANT_BOX for the 'up' command
#    VAGRANT_BOX=scm-manager-builder vagrant up
# If you want to re-run the provision $script
#    vagrant reload --provision

vagrantBox = ENV['VAGRANT_BOX']
if not vagrantBox
  vagrantBox = 'centos/7'
end

$script = <<HEREDOC
echo "Provisioning script running as $(whoami)"
echo "Install packages required to build and host scm-manager"
yum install -y java-1.7.0-openjdk-devel maven mercurial
HEREDOC


Vagrant.configure(2) do |config|
  config.vm.box = vagrantBox
  config.vm.define 'scm-manager-builder'
  config.vm.provider 'virtualbox' do |provider_vm|
    provider_vm.name = 'scm-manager-builder'
    provider_vm.memory = 4096
    # Compiling is CPU intensive; increase if necessary
    provider_vm.cpus = 4
  end
  config.vm.network "forwarded_port", guest: 8081, host: 8081
  # Default Vagrant rsync of the local folder used to avoid build issues
  config.vm.synced_folder ".", "/vagrant", disabled: true
  # Create mounted folder instead; used to copy build artifacts back out to developer host
  # REQUIRES: vagrant plugin 'vagrant-vbguest'
  config.vm.synced_folder ".", "/scm-manager", automount: true, mount_options: ["dmode=770,fmode=660"]

  config.vm.provision :shell, inline: $script
end