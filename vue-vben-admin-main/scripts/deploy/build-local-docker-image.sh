#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR=$( cd -- "${SCRIPT_DIR}/../.." &> /dev/null && pwd )
LOG_FILE=${SCRIPT_DIR}/build-local-docker-image.log
ERROR=""
APP_NAME="${1:-web-ele}"
IMAGE_NAME="${2:-vben-${APP_NAME}-local}"

case "${APP_NAME}" in
    web-ele|web-portal)
        ;;
    *)
        >&2 echo "ERROR: APP_NAME must be web-ele or web-portal"
        exit 1
        ;;
esac

function stop_and_remove_container() {
    # 停止并删除同名本地容器。
    docker stop "${IMAGE_NAME}" >/dev/null 2>&1
    docker rm "${IMAGE_NAME}" >/dev/null 2>&1
}

function remove_image() {
    # 删除同名本地镜像，避免误用旧产物。
    docker rmi "${IMAGE_NAME}" >/dev/null 2>&1
}

function install_dependencies() {
    # 安装 workspace 依赖。
    cd "${ROOT_DIR}" || exit 1
    pnpm install || ERROR="install_dependencies failed"
}

function build_image() {
    # 按应用名构建独立镜像。
    docker build "${ROOT_DIR}" -f "${SCRIPT_DIR}/Dockerfile" --build-arg VITE_APP="${APP_NAME}" -t "${IMAGE_NAME}" || ERROR="build_image failed"
}

function log_message() {
    if [[ ${ERROR} != "" ]];
    then
        >&2 echo "build failed, Please check build-local-docker-image.log for more details"
        >&2 echo "ERROR: ${ERROR}"
        exit 1
    else
        echo "docker image with tag '${IMAGE_NAME}' for '${APP_NAME}' built successfully. Use below sample command to run the container"
        echo ""
        echo "docker run -d -p 8010:8080 --name ${IMAGE_NAME} ${IMAGE_NAME}"
    fi
}

echo "Info: Stopping and removing existing container and image" | tee "${LOG_FILE}"
stop_and_remove_container
remove_image

echo "Info: Installing dependencies" | tee -a "${LOG_FILE}"
install_dependencies 1>> "${LOG_FILE}" 2>> "${LOG_FILE}"

if [[ ${ERROR} == "" ]]; then
    echo "Info: Building docker image for ${APP_NAME}" | tee -a "${LOG_FILE}"
    build_image 1>> "${LOG_FILE}" 2>> "${LOG_FILE}"
fi

log_message | tee -a "${LOG_FILE}"
