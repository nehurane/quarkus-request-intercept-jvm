#!/bin/bash -e

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
export ROOT_PROJECT_DIR="${script_dir}/../../"
export PROJECT_DIR="${script_dir}/../"
export PROJECT
PROJECT=$(basename $(cd $PROJECT_DIR && pwd))
export JAVA_HOME=$JAVA17_HOME

alias project_gradlew="./gradlew --debug"

function get_region_dirname() {
  local region=$(echo "$1" | tr '[:upper:]' '[:lower:]')
    case $region in
      eucentral1)
        echo "eu-central-1" ;;
      euwest1)
        echo "eu-west-1" ;;
      useast2)
        echo "us-east-2" ;;
      uswest2)
        echo "us-west-2" ;;
      *)
        echo "Region not supported"
        return 1
    esac
    return 0
}

should_deploy_new_version() {
  local app=$1
  local env=$2
  local repo=$3

  set +e
  local currently_deployed_version="$(get_deployed_image_version "$app" "$env")"
  set -e
  local version_to_deploy="$(get_latest_image_version "$app" "$repo")"

  _should_deploy_version "$currently_deployed_version" "$version_to_deploy"
}

should_deploy_new_canary_version() {
  local app=$1
  local env=$2
  local repo=$3

  set +e
  local currently_deployed_version="$(get_deployed_canary_image_version "$app" "$env")"
  set -e
  local version_to_deploy="$(get_latest_image_version "$app" "$repo")"

  _should_deploy_version "$currently_deployed_version" "$version_to_deploy"
}

_should_deploy_version(){
  local currently_deployed_version="$1"
  local version_to_deploy="$2"
  echo "currently_deployed_version: ${currently_deployed_version}, version_to_deploy: ${version_to_deploy}"

  if [[ "$version_to_deploy" != "$currently_deployed_version" ]]; then
      echo "Deploy version ${version_to_deploy}"
      checkout_latest_image_revision "$app" "$repo"
      return 0
  else
      echo "$app currently_deployed_version and version_to_deploy are the same. should skip deploy!"
      return 1
  fi
}

wait_and_check(){
  local process_pid=$1
  local namespace=$2
  local cluster=$3

  wait "$process_pid" && process_exit_code=$?
  if [[ $process_exit_code != 0 ]]; then
      log_k8s_events "$cluster" "$namespace"

      return 1
  fi
}

function get_secrets_path {
  local org="$1"
  local org_lowercase=$(tr "[:upper:]" "[:lower:]" <<< "$org")
  local region="$2"
  local namespace="$3"
  local appName="$4"

  echo "$ROOT_PROJECT_DIR/vidar-sandbox-env/$appName/$org_lowercase/$(get_region_dirname "$region")/secrets"
}

function get_default_nft_region {
  local org=$(echo "$1" | tr '[:upper:]' '[:lower:]')
    case $org in
      summer)
        echo "UsEast2"
      ;;
      skyglobal)
        echo "EuCentral1"
      ;;
      nbcu)
        echo "UsWest2"
      ;;
      *)
        return 1
      ;;
    esac
}

function get_default_nft_cluster {
  local org="$1"
  local region="$2"
  get_k8s_context "dev" "$org" "$region"
}

function get_default_region {
  local org=$(echo "$1" | tr '[:upper:]' '[:lower:]')
  case $org in
    summer)
      echo "UsEast2"
      ;;
    skyglobal)
      echo "EuCentral1"
      ;;
    nbcu)
      echo "UsWest2"
      ;;
    *)
      return 1
      ;;
  esac
}

deploy_app_to() {
  local org="$1"
  local region="$2"
  local env="$3"
  local namespace="$4"
  local cluster="$5"
  local notify="$6"
  local version="$7"

  apply-secrets.sh -c "$cluster" \
    -d "$(get_secrets_path "$org" "$region" "$namespace" "vidar-sandbox-service")" \
    -n "$namespace" \
    -k "$SECRETS_KEY"
  local apply_secrets_exit_code=$?
  echo "Apply secrets exit code: $apply_secrets_exit_code"

  if [[ $notify = true ]]; then
    shift 7
    deploy_to "$PROJECT" "$env" true "$namespace-$org-$region" "$version" "sas-vidar-help" "$@" &
  elif [[ $notify = false ]]; then
    shift 6
    deploy_to "$PROJECT" "$env" false "$@" &
  else
    shift 5
    deploy_to "$PROJECT" "$env" false "$@" &
  fi
  wait_and_check $! "$namespace" "$cluster"
  deploy_exit_code=$?
  echo "Deployment exit code for vidar-sandbox-service: $deploy_exit_code"
  return $deploy_exit_code
}

function getVersionToDeploy() {
    local defaultVersion="$1"
    if [[ -z ${DEPLOY_VERSION} || ${DEPLOY_VERSION} == "latest" ]]; then
        DEPLOY_VERSION=$defaultVersion
    elif [[ ${DEPLOY_VERSION} == v* ]]; then
        DEPLOY_VERSION=${DEPLOY_VERSION:1}
    fi
    echo -n "${DEPLOY_VERSION}"
}