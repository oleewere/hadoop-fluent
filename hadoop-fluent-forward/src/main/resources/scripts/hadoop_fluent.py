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
import time
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

def validate(options, parser):
  if not options.storage_type:
    error(parser, 'Storage type paramter is required (use -t or --storage-type)')

def start(options):
  _, hadoop_fluent_exists = hadoop_fluent_process_exists(options)
  if hadoop_fluent_exists:
    print "hadoop-fluent application is already running."
    return
  
  storage_libs_path = os.path.join(libs_folder, options.storage_type)
  conf_folder = _conf_folder(options)
  classpath = "{0}/core/*:{1}/*:{2}:{3}".format(libs_folder, storage_libs_path, conf_folder, _core_site_folder(options))
  java_opts = "-Dhadoop.fluent.app.name=hadoop-fluent"
  if options.server:
    java_opts = "{0} -Dhadoop.fluent.global.mode=server".format(java_opts)
  if is_str_not_empty(options.archive_base_dir):
    java_opts = "{0} -Dhadoop.fluent.global.archive_base_dir={1}".format(java_opts, options.archive_base_dir)
  java_opts = "{0} {1}".format(java_opts, options.extra_java_opts) if options.extra_java_opts else "{0}".format(java_opts)
  conf_file = os.path.join(conf_folder, "hadoop-fluent.conf")
  start_command = '{0} -classpath "{1}" {2} Main {3}'.format(_java_bin(options), classpath, java_opts, conf_file)
  print "Start process with the following command: {0}".format(start_command)
  start_cmd(start_command, options)

  if not options.foreground:
    print "Waiting a few seconds until hadoop-fluent is started ..."
    time.sleep(5)

    pid = get_hadoop_fluent_pid_by_ps_grep()
    if is_str_not_empty(pid):
      print "hadoop-fluent application has started with pid: {0}.".format(pid)
      return
    else:
      print "hadoop-fluent application start failed."
      if os.path.exists(pidfile_path):
        os.remove(pidfile_path)
      sys.exit(1)

def start_cmd(command, options):
  pid_file = os.path.join(_pid_dir(options), "hadoop_fluent.pid")
  if options.foreground:
    os.system(command)
  else:
    os.system("nohup {0} >/dev/null 2>&1 & echo $! > {1}".format(command, pid_file))

def hadoop_fluent_process_exists(options):
  pidfile_path = os.path.join(_pid_dir(options), "hadoop_fluent.pid")
  if os.path.exists(pidfile_path):
    _, _, pid_resp = run_command("cat {0}".format(pidfile_path))
    if pid_resp:
      pid = pid_resp.split('\n', 1)[0]
      _, ret, _ = run_command("kill -0 {0}".format(pid))
      if ret == 0:
        return pid, True
      else:
        return check_pid_with_ps(options, pidfile_path, pid)
    else:
      return check_pid_with_ps(options, pidfile_path)
  else:
    return check_pid_with_ps(options, pidfile_path)

def get_hadoop_fluent_pid_by_ps_grep():
  p1 = subprocess.Popen(('ps', '-A'), stdout=subprocess.PIPE)
  p2 = subprocess.Popen(('grep', "hadoop.fluent.app.name"), stdin=p1.stdout, stdout=subprocess.PIPE)
  p3 = subprocess.Popen(('grep', "-v", "grep"), stdin=p2.stdout, stdout=subprocess.PIPE)
  p4 = subprocess.Popen(('awk', '{print $1}'), stdin=p3.stdout, stdout=subprocess.PIPE)
  p1.wait()
  p2.wait()
  p3.wait()
  p4.wait()
  pid = p4.stdout.read()
  return pid.strip()

def check_pid_with_ps(options, pidfile_path, wrong_pid=None):
  pid = get_hadoop_fluent_pid_by_ps_grep()
  if is_str_not_empty(pid):
    if wrong_pid:
      print "Pid file {0} contains wrong pid: {1}".format(pidfile_path, wrong_pid)
      os.remove(pidfile_path)
    print "Found existing pid file for hadoop-fluent application. Creating a new one with pid {0}.".format(pid)
    with open(pidfile_path,'w') as f:
      f.write(pid)
    return pid, True
  else:
    return None, False

def is_str_not_empty(str):
  if str and (not str.isspace()):
    return True
  else:
    return False

def run_command(command):
  p = subprocess.Popen(command.split(), stdout=subprocess.PIPE)
  out, err = p.communicate()
  return_code = p.returncode
  return p, return_code, out

def status(options):
  _, hadoop_fluent_exists = hadoop_fluent_process_exists(options)
  if hadoop_fluent_exists:
    print "hadoop-fluent appliaction is running."
  else:
    print "hadoop-fluent appliaction is not running."
    sys.exit(1)

def stop(options):
  pid, hadoop_fluent_exists = hadoop_fluent_process_exists(options)
  if hadoop_fluent_exists:
    print "hadoop-fluent is running with pid {0}. Wait for stopping ...".format(pid)
    run_command("kill {0}".format(pid))
    
    def check_process_is_stopped(max_tries = 30, sleep = 5):
      tries = 0
      while tries <= max_tries:
        pid = get_hadoop_fluent_pid_by_ps_grep()
        if is_str_not_empty(pid):
          tries = tries + 1
          sys.stdout.write(".")
          sys.stdout.flush()
          time.sleep(sleep)
        else:
          return
      print "\nhadoop-fluent could not be stopped after in time. (After {0} tries)".format(max_tries)
      sys.exit(1)
    check_process_is_stopped(30, 5)
    print "hadoop-fluent application is stopped."
  else:
    print "hadoop-fluent application is not running, nothing to stop."
  pidfile_path = os.path.join(_pid_dir(options), "hadoop_fluent.pid")
  if os.path.exists(pidfile_path):
    os.remove(pidfile_path)

def restart(options):
  stop(options)
  start(options) 

def error(parser, message):
  print message
  parser.print_help()
  sys.exit(1)

if __name__=="__main__":
  parser = optparse.OptionParser("usage: %prog [options]")
  parser.add_option("-A", "--action", dest="action", type="string", help="action: start | stop | restart | status")
  parser.add_option("-s", "--server", dest="server", action="store_true", help="Run hadoop-fluent application with forwarder input")
  parser.add_option("-t", "--storage-type", dest="storage_type", type="string", help="storage type: s3 | abfs | wasb | gcs | hdfs")
  parser.add_option("-d", "--archive-base-dir", dest="archive_base_dir", type="string", help="Base directory for hadoop-fluent uploader thread.")
  parser.add_option("-H", "--hadoop-conf", dest="hadoop_conf", type="string", help="hadoop conf folder that contains the core-site.xml file")
  parser.add_option("-c", "--conf", dest="conf", type="string", help="custom path for the hadoop-fluent configuration file")
  parser.add_option("-j", "--java-home", dest="java_home", type="string", help="Java home that is used for hadoop-fluent java application")
  parser.add_option("-o", "--extra-java-opts", dest="extra_java_opts", type="string", help="Additional java options for hadoop-fluent java application")
  parser.add_option("-p", "--pid-dir", dest="pid_dir", type="string", help="Pid directory for hadoop-fluent java application.")
  parser.add_option("-f", "--foreground", dest="foreground", action="store_true", help="Run application in foreground.")
  parser.add_option("--fluentd-agent", dest="use_fluentd_agent", action="store_true", help="manage fluentd agent application")
  parser.add_option("--td-agent", dest="use_td_agent", action="store_true", help="manage td agent application")
  parser.add_option("-v", "--verbose", dest="verbose", action="store_true", help="use for verbose logging")
  (options, args) = parser.parse_args()

  if options.action == "start":
    print "Starting hadoop-fluent application ..."
    validate(options, parser)
    start(options)
  elif options.action == "stop":
    print "Stopping hadoop-fluent application ..."
    stop(options)
  elif options.action == "restart":
    print "Restarting hadoop-fluent application ..."
    validate(options, parser)
    restart(options)
  elif options.action == "status":
    status(options)
  elif options.action is None:
    error(parser, "Parameter 'action' is missing!")
  else:
    error(parser, "Not a valid 'action': {0}".format(options.action))