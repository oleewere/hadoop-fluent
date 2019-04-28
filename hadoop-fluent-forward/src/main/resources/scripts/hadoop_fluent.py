#!/usr/bin/python

'''
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

import optparse
import os
import subprocess
import sys
import ConfigParser

dirname=os.path.dirname
root_folder = os.path.join(dirname(dirname(__file__)))
libs_folder = os.path.join(root_folder, "libs")

def _java_bin(options):
  if options.java_home:
    return os.path.join(options.java_home, "bin", "java")
  else: 
    return "java" if os.environ.get('JAVA_HOME') is None else os.path.join(os.environ.get('JAVA_HOME'), "bin", "java")

def _conf_folder(options):
  return options.conf if options.conf else os.path.join(root_folder, "conf")

def _core_site_folder(options):
  return options.hadoop_conf if options.hadoop_conf else os.path.join(root_folder, "conf")

def _pid_dir(options):
  return options.pid_dir if options.pid_dir else root_folder

def validate(options):
  pass

def start(options):
  storage_libs_path = os.path.join(libs_folder, options.storage_type)
  conf_folder = _conf_folder(options)
  classpath = "{0}/core/*:{1}/*:{2}:{3}".format(libs_folder, storage_libs_path, conf_folder, _core_site_folder(options))
  java_opts = ""
  conf_file = os.path.join(conf_folder, "hadoop-fluent.conf")
  start_command = '{0} -classpath "{1}" {2} com.cloudera.hadoop.cloud.Main {3}'.format(_java_bin(options), classpath, java_opts, conf_file)
  print "Start process with the following command: {0}".format(start_command)
  start_cmd(start_command, options)

def start_cmd(command, options):
  pid_file = os.path.join(_pid_dir(options), "hadoop_fluent.pid")
  if options.foreground:
    pid = os.getpid()
    print pid
    os.system(command)
  else:
    os.system("nohup {0} >/dev/null 2>&1 & echo $! > {1}".format(command, pid_file))

def error(parser, message):
  print message
  parser.print_help()
  sys.exit(1)

if __name__=="__main__":
  parser = optparse.OptionParser("usage: %prog [options]")
  parser.add_option("-A", "--action", dest="action", type="string", help="action: start | stop | restart | status")
  parser.add_option("-s", "--storage-type", dest="storage_type", type="string", help="storage type: s3 | abfs | wasb | gcs | hdfs")
  parser.add_option("-H", "--hadoop-conf", dest="hadoop_conf", type="string", help="hadoop conf folder that contains the core-site.xml file")
  parser.add_option("-c", "--conf", dest="conf", type="string", help="custom path for the hadoop-fluent configuration file")
  parser.add_option("-j", "--java-home", dest="java_home", type="string", help="Java home that is used for hadoop-fluent java application")
  parser.add_option("-p", "--pid-dir", dest="pid_dir", type="string", help="Pid directory for hadoop-fluent java application.")
  parser.add_option("-f", "--foreground", dest="foreground", type="string", help="Run application in foreground.")
  parser.add_option("--fluentd-agent", dest="use_fluentd_agent", action="store_true", help="manage fluentd agent application")
  parser.add_option("--td-agent", dest="use_td_agent", action="store_true", help="manage td agent application")
  parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="use for verbose logging")
  (options, args) = parser.parse_args()

  if options.action == "start":
    validate(options)
    start(options)
    pass
  elif options.action == "stop":
    pass
  elif options.action == "restart":
    pass
  elif options.action == "status":
    pass
  elif options.action is None:
    error(parser, "Parameter 'action' is missing!")
  else:
    error(parser, "Not a valid 'action': {0}".format(options.action))