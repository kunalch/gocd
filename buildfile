#*************************GO-LICENSE-START********************************
# Copyright 2014 ThoughtWorks, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or a-qgreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#*************************GO-LICENSE-END**********************************

require 'java' if RUBY_PLATFORM == "java"
$PROJECT_BASE = File.expand_path("../", __FILE__)

RUNNING_TESTS = 'running_tests'

def running_tests?
  ENV[RUNNING_TESTS] == 'true'
end

def running_tests!
  ENV[RUNNING_TESTS] = 'true'
end

def not_running_tests!
  ENV[RUNNING_TESTS] = 'false'
end

# Generated by Buildr 1.3.4, change to your liking
# Version number for this release
VERSION_NUMBER = if ENV['GO_DIST_VERSION']
                   "15.3.0-#{ENV['GO_DIST_VERSION']}"
                 else
                   '15.3.0'
                 end

# Group identifier for your projects
GROUP = "cruise"

#discover the revision and commit digest
def stdout_of command
  Util.win_os? && command.gsub!(/'/, '"')
  stdout = `#{command}`
  $?.success? || fail("`#{command}` failed")
  stdout
end

vcs_rev_13=stdout_of("git log -1 --pretty='%H'")[0..13]
vcs_rev_short=stdout_of("git log -1 --pretty='%h'")
RELEASE_VCS_REV = (vcs_rev_short.length > vcs_rev_13.length) ? vcs_rev_short : vcs_rev_13
RELEASE_REVISION = stdout_of("git log --pretty=format:''").length
RELEASE_COMMIT = "#{RELEASE_REVISION}-#{RELEASE_VCS_REV}"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://repo1.maven.org/maven2/"

desc "Go - ThoughtWorks Studios"
define "cruise" do |project|
  compile.options[:other] = %w[-encoding UTF-8 -target 1.7 -source 1.7]
  TMP_DIR = test.options[:properties]['java.io.tmpdir'] = _('target/temp')
  mkpath TMP_DIR

  manifest['Go-Version'] = VERSION_NUMBER

  project.version = VERSION_NUMBER
  project.group = GROUP

  ENV["VERSION_NUMBER"] = VERSION_NUMBER
  ENV["RELEASE_COMMIT"] = RELEASE_COMMIT

  require './cruise-modules'

  desc "bump version number"
  task :update_versions do
    bump_command = "mvn versions:set -DnewVersion=#{VERSION_NUMBER} -DgenerateBackupPoms=false"
    sh(bump_command)
  end

  clean do
    mkpath TMP_DIR
  end
end

