#!/bin/bash

readonly PROGNAME=`basename "$0"`
readonly SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
readonly TOPLEVEL_DIR=$( cd ${SCRIPT_DIR}/../.. > /dev/null && pwd )

####################
# GLOBAL VARIABLES #
####################

FLAG_DRYRUN=false
FLAG_QUIET=false
FLAG_FORCE=false



##########
# SOURCE #
##########

for functionFile in ${TOPLEVEL_DIR}/helm/scripts/functions/*.active;
  do source ${functionFile}
done

##########
# SCRIPT #
##########

usage_message () {
  echo """Usage:
    $PROGNAME --resource RESOURCE [--resource RESOURCE] [OPT ..]
      -r | --resource) ... multiple definitions possible

      -f | --force)    ... process template even if target file exists
      -d | --dryrun)   ... dryrun
      -q | --quiet)    ... quiet

      -h | --help)     ... help"""
}
readonly -f usage_message
[ "$?" -eq "0" ] || return $?

main() {
    local resources=()
    local flag_install=false
    local flag_upgrade=false
    local flag_uninstall=false

     # getopts
  local opts=`getopt -o hfr:q --long git-branch:,oc-admin-token:,oc-cluster:,namespace:,help,force,dryrun,resource:,quiet -- "$@"`
  local opts_return=$?
  if [[ ${opts_return} != 0 ]]; then
      echo
      (>&2 echo "failed to fetch options via getopt")
      echo
      return ${opts_return}
  fi
  eval set -- "$opts"
  while true ; do
      case "$1" in
      --resource)
          resource=${2,,}
          shift 2
          ;;
      --oc-admin-token)
          oc_admin_token=$2
          shift 2
          ;;
      --oc-cluster)
          oc_cluster=$2
          shift 2
          ;;
      --namespace)
          namespace=$2
          shift 2
          ;;
      --git-branch)
          git_branch=$2
          shift 2
          ;;
      -h | --help)
          usage_message
          exit 0
          ;;
      -f | --force)
          FLAG_FORCE=true
          shift
          ;;
      --dryrun)
          FLAG_DRYRUN=true
          shift
          ;;
      -q | --quiet)
        FLAG_QUIET=true
        shift
        ;;
      *)
          break
          ;;
      esac
  done
}
readonly -f main
[ "$?" -eq "0" ] || return $?

main $@