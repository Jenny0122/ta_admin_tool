/**
 *
 * @param {Number} collectionId
 * @param {'SKIP'|'STOP'} errorHandle
 */
const requestImport = function requestAsnycImportDocument(collectionId, errorHandle = "SKIP") {

    // [0] 버튼 비활성화 - 업로드, 삭제, 파일 선택, 파일 분석
    UploadPollingStatus.start();

    // [1] get field that user-checked
    const fieldNames = [];
    const fieldChecks = [];
    for (let checkbox of [...document.querySelectorAll("[name=field][type=checkbox]")]) {
        fieldNames.push(checkbox.value);
        fieldChecks.push(checkbox.checked);
    }

    // [2] request import ( collectionId, exceptionHandle, fieldanmes[])
    console.debug("async-import-document. errorHandle, fieldNames, fieldChecks:", errorHandle, fieldNames, fieldChecks);

    $.ajax({
        type: "POST",
        url: `${contextPath}/documentRest/imports`,
        data: {errorHandle: errorHandle.toUpperCase(), fieldNames: fieldNames, fieldChecks: fieldChecks, collectionId: collectionId},
        success: function (links, textStatus, request) {
            console.debug("callback links: ", links);

            // if export-request accepted, start polling
            checkProgress(collectionId);

            window.scrollTo(0, 0);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            UploadPollingStatus.stop();
            console.error(`Failed to request export collection-${collectionId}. cause=${jqXHR.responseText}`);
            // console.debug(jqXHR);

            switch (jqXHR.status) {
                case 400:
                    alert("업로드 필수 파라미터(필드, 오류처리) 확인해주세요.\n" +
                        `"${jqXHR.responseText}"`);

                    if (!fieldNames || !fieldNames.includes("DOCID") || !fieldChecks[fieldNames.indexOf("docid")]) {
                        console.error("'docid' missing");
                        document.getElementById("table-document").scrollIntoView();
                    } else {
                        console.error("Invalid file exist.");
                        const invalidFileElement = [...document.querySelectorAll(".tbody-staged .uploadState")]
                            .find(value => value.innerText !== "-");
                        (invalidFileElement || document.body).scrollIntoView();
                    }

                    UploadPollingStatus.reset();
                    break;
                case 409:
                    console.error(`failed to request upload. ${jqXHR.responseText}`);
                    checkProgress(collectionId);
                    setTimeout(function () {
                        alert(`${jqXHR.responseText}`);
                    }, 500);
                    break;
                default:
                    alert(`업로드 요청 실패.\n${jqXHR.responseText}`);
                    break;
            }
            throw "Failed to request";
        }
    });
};

/**
 * 페이지 진입시, 업로드가 진행중인지 체크.
 * 진행 중이라면 바로 업로드 진행 상태로 변경.
 */
const checkIfInProgress = function checkIfInProgressWhenPageEntry() {

    // get collectionId from url-path
    const collectionId = /\/collection\/(\d+)\/upload/.exec(window.location.pathname)[1];

    $.ajax({
        async: false,
        type: "GET",
        url: `${contextPath}/documentRest/${collectionId}/importStatus`,
        timeout: 1000,
        success: function (status, textStatus, jqXHR) {
            // if in progress
            if (jqXHR.status === 202) {
                if (!UploadPollingStatus.isInProgress()) {
                    UploadPollingStatus.start();
                }
                // start checkProgress
                checkProgress(collectionId);

                alert("업로드가 진행중입니다.");
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.debug(jqXHR);
        }
    });
};

/**
 * 비동기 문서 업로드의 진행 상황을 주기적으로 체크
 */
const checkProgress = function checkAsyncDocumentImportProgress(collectionId) {
    const COMPLETE_STATE = new Set(["PARTIAL_SUCCESS", "FAILURE"]); // "SUCCESS", 전체 완료는 로그 필요 없음

    const STATE_LABEL = {
        WAITING: "대기중",
        IN_PROGRESS: "진행중",
        SUCCESS: "전체 성공", 
        PARTIAL_SUCCESS: "부분 성공", 
        FAILURE: "실패",
    };

    // fileName : stateElements
    let fileStateElement = {};
    [...document.querySelectorAll("#table-staged tbody tr.tbody-staged")].forEach((tr, index) => {
        fileStateElement[tr.querySelector(".fileName").innerText] = tr.querySelector(".uploadState");
    });

    /**
     *
     * @param fileStateElement
     * @param {Array} statusList
     * @param {string} statusList.fileName
     * @param {string} statusList.progress
     * @param {string} statusList.totalCount
     * @param {string} statusList.validCount
     *
     */
    const refreshProgress = function refreshImportProgresStatus(fileStateElement, statusList) {
        for (let progress of statusList) {
            const element = fileStateElement[progress.fileName];

            if (element) {
                element.innerText = `${STATE_LABEL[progress.progress]} (${progress.validCount} / ${progress.totalCount})`;

                if (COMPLETE_STATE.has(progress.progress)) {
                    element.innerHTML += `<a><i onclick="historyPopup(this.dataset)" class="fa-exclamation-circle fas ml5" data-resourceid="${progress.resourceId}" data-filename="${progress.fileName}" style="color: red"></i></a>`
                }
            }
            // if element is undefined, maybe file already removed
            // console.debug(`Can not found '${progress.fileName}' at tbody-staged.`);
        }
    };

    // [3] interval status check
    // [3-1] in interval, 202 Accept - update status, remove button-delete
    // [4] 200 OK? 201 created? - alert completion,
    const statusCheckInterval = setInterval(function asyncImportStatusCheck() {
        $.ajax({
            async: false,
            type: "GET",
            url: `${contextPath}/documentRest/${collectionId}/importStatus`,
            timeout: 1000,
            /**
             * @param {Array} status
             * @param {string} status.fileName
             * @param {string} status.status
             */
            success: function (status, textStatus, jqXHR) {
                switch (jqXHR.status) {
                    case 202:
                        refreshProgress(fileStateElement, status);
                        break;
                    case 200:
                        console.info("successfully imported. ", jqXHR);
                        $.get(`/collectionRest/importHistory?collectionId=` + collectionId)
                            .then(function (history) {
                                refreshProgress(fileStateElement, history);
                            });
                    default:
                        clearInterval(statusCheckInterval);
                        UploadPollingStatus.stop();
                        console.log("End import-status polling.");
                        setTimeout(function () {
                            alert("문서 업로드가 완료되었습니다. 결과를 확인해주세요");
                            location.reload();
                        }, 200);
                        break;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {

            }
        });

    }, 1000);

    // console.debug(`statusCheckInterval : ${statusCheckInterval}`);

    // 일단은 3분 제한
    setTimeout(function () {
        // console.debug(statusCheckInterval);
        if (statusCheckInterval) {
            clearInterval(statusCheckInterval);
        }
    }, 1000 * 60 * 3); // 3 min
};


/**
 * 업로드 히스토리 팝업을 띄워서 디테일 로그를 보여준다.
 * @param {DOMStringMap} dataset
 */
const historyPopup = function popupTableUploadHistoryEachFile(id, dataset) {
    if (!dataset.hasOwnProperty("resourceid")) {
        throw "check param[resourceId]";
    }
    if (!dataset.hasOwnProperty("filename")) {
        throw "check param[fileName]";
    }
    let elemResultTableBody = document.querySelector("#result-data tbody");

    $.ajax({
        type: "GET",
        url: `${contextPath}/collectionRest/importHistoryData?collectionId=${dataset.resourceid}`,
        data: {
				importId : id
			  , fileName: dataset.filename
			  },
        success: function (history, status, jqhXHR) {
            // console.group(`Collection[id=${dataset.resourceid}]'s Document upload history of '${dataset.filename}'`);
            // console.table(history.log);
            // console.groupEnd();

            // document.getElementById("result-title").innerText = `${history.progress} ( ${history.validCount} / ${history.totalCount} ), 오류 문서 처리: ${history.errorHandle}`;
            document.getElementById("result-title").innerText = `오류 로그 [${history.fileName}]`;
            document.getElementById("result-title").title =
                `업로드 일시 : ${new Date(history.importDt)}\n` +
                `오류 문서 처리 : ${history.errorHandle === "SKIP" ? "건너뛰기" : "파일 업로드 중지"}\n` +
                `업로드 파일 크기 : ${history.fileSize} byte\n` +
                `파싱 가능한 문서 : ${history.totalCount}건\n` +
                `업로드 된 문서 : ${history.validCount}건\n`;

            document.getElementById("result-download").addEventListener("click", function (event) {
                const a = document.createElement("a");
                const p = new URLSearchParams({"importId":id, "fileName": dataset.filename});
                a.href = `${contextPath}/collectionRest/${dataset.resourceid}/importHistoryLogs?${p.toString()}`;
                document.body.append(a);
                a.click();
                a.remove();
            });
            document.getElementById("result_pop").style.display = "";

            let innerHTML = "";
            var log = history.logText.split('\\|');
            
            if (Array.isArray(log) && log.length > 0) {
                let size = log.length;
                for (let idx = 0; idx < size; idx++) {
                    innerHTML += `<tr><td>${idx + 1}</td><td class="field-log">${log[idx]}</td></tr>`;

                    if (idx >= 20) {
                        innerHTML += `<tr><td colspan="2">...</td></tr>`;
                        innerHTML += `<tr><td colspan="2">${size}건의 전체 로그는 <a href="#result-download">로그 파일을 다운</a>받아 확인해주시길 바랍니다.</td></tr>`;
                        break;
                    }
                }
            } else {
                innerHTML = `<tr><td colspan="2">업로드 오류 로그가 존재하지 않습니다.</td></tr>`;
            }
            elemResultTableBody.innerHTML = innerHTML;

        },
        error: function (request, status, error) {
            console.error(error);
            alert("업로드 결과를 가져오는데 실패했습니다.");
        }
    });
    // end ajax


};

/**
 * Collection 파일 업로드
 * @param {Number} collectionId Collection's id
 * @param {Element} formElement multipart upload form
 */
const uploadFile = function uploadFileIntoImportCollection(collectionId, formElement) {
    const formData = new FormData(formElement);
    const file = formData.get("file");
    const maxFileSize = formElement.dataset["maxFileSize"] || 1024 * 1024 * 100; // 서버 설정 이 안넘어올 경우 100 MB
    const FILE_SIZE_LIMIT = 1024 * 1024 * 1024 * 2; // 2GB

    console.debug(`tm-v3-manager.fileUpload.maxFileSize=${maxFileSize}, FILE_SIZE_LIMIT(high priority)=${FILE_SIZE_LIMIT}`);

    try {
        if (!validFileType(file.name)) {
            alert("사용할 수 없는 파일 확장자 입니다.");
            throw `사용할 수 없는 파일 확장자 ${file.name}`;
        }
        if (file.size > maxFileSize) { // maxFileSize
            alert(`${get_file_size(maxFileSize)} 보다 큰 파일은 업로드 할 수 없습니다. 관리자에게 문의 바랍니다.`);
            throw `파일 크기 초과 ${file.size}`;
        }
        if (file.size > FILE_SIZE_LIMIT) { // 2 GB
            alert(`단일 문서 파일이 ${get_file_size(FILE_SIZE_LIMIT)} 를 초과할 수 없습니다.`);
            throw `파일 크기 초과 ${file.size}`;
        }

        if (file.size === 0) {
            alert("파일 크기가 올바르지 않습니다.");
            throw `파일 크기 미달 ${file.size}`;
        }

    } catch (e) {
        console.error(e);
        console.debug("file: ", file);
        formElement.reset();
        return false;
    }

    $.ajax({
        async: false,
        type: "GET",
        url: contextPath + '/collectionRest/importHistory?collectionId=' + collectionId,
        data: {fileName: file.name},
        beforeSend: function (jqXHR, settings) {
        },
        success: function (response) {
            // 업로드 이력 존재 --> 업로드 금지
            if (response.result == "S") {
				$.ajax({
                    type: "POST",
                    enctype: 'multipart/form-data',
                    url: `${contextPath}/file/import/collection/${collectionId}/`,
                    data: formData,
                    processData: false,
                    contentType: false,
                    cache: false,
                    // no timeout //timeout: 3 * 60 * 1000, // ms
                    beforeSend: function () {
                        console.group("file-upload");
                        console.table({file_name: file.name, file_size: get_file_size(file.size)});
                        console.time("file-upload");
                        [...document.forms.namedItem("file-upload-form").elements]
                            .forEach(e => e.disabled = true);
                    },
                    xhr: function () {
                        let xhr = $.ajaxSettings.xhr();
                        xhr.upload.onprogress = function (progressEvent) {
                            try {
                                const percent = Math.floor(progressEvent.loaded * 100 / progressEvent.total);
                                document.querySelector("#upload-progress").innerHTML = `진행중... ${percent} %`;
                                console.debug(file.name, " 파일 업로드 진행률... ", percent);
                                if (percent === 100) {
                                    document.querySelector("#upload-progress").innerHTML = "완료중... 잠시만 기다려주세요";
                                }
                            } catch (e) {
                                console.debug(file.name, " 파일 업로드 진행률... ", progressEvent.loaded, " / ", progressEvent.total);
                            }
                        };
                        return xhr;
                    },
                    success: function (response) {
                        document.querySelector("#upload-progress").innerHTML = `파일 추가 완료 ${file.name} ${get_file_size(file.size)}`;
                        setTimeout(function () {
                            alert("파일 추가 완료");
                        }, 300);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.error("file uplaod failed. statusText=", textStatus, ", jqXHR=", jqXHR);
                        if (status === 0) {
                            if (textStatus === "timeout") {
                                // alert("타임아웃이 발생했습니다. 파일을 분할하여 업로드 바랍니다.");
                                // return;
                            }
                        }
                        // alert("네트워크가 불안정하여 파일을 업로드 할 수 없습니다.");
                        alert(jqXHR.responseText);
                    },
                    complete: function (response) {
                        console.timeEnd("file-upload");
                        location.reload();
                    }
                });
			} else {
	            alert("업로드 이력이 존재하는 파일입니다.");
			}
        },
        complete: function () {
            console.groupEnd();
        }
    });

    return false;
};


/**
 * 파일 확장자 validation
 */
const validFileType = function (file_name) {
//    const regexFileExtensions = /^(.*\.(scd|csv|json)$)/gim; // allow .scd, json, csv
    const regexFileExtensions = /^(.*\.(csv|json)$)/gim; // allow .json, csv
    if (!regexFileExtensions.test(file_name)) {
        return false;
    }
    return true;
};


/**
 * upload 돼있는 파일 삭제
 * @param collectionId
 * @param elementTableRow
 * @returns {boolean}
 */
const deleteFile = function deleteFileAtImportColleciton(collectionId, fileName) {
    if (!fileName) {
        console.error('fileName 확인');
        return false;
    }

    if (!confirm(`${fileName} 을 삭제하시겠습니까?`)) {
        return false;
    }

    $.ajax({
        type: "DELETE",
        url: `${contextPath}/file/import/collection/${collectionId}/${fileName}`,
        success: function (response) {
            // collection 삭제 성공
            console.log(`파일 삭제 성공: ${response}`);

            alert(`파일이 삭제되었습니다.`);

            location.reload(true);
        },
        error: function (data) {
            console.error("Failed to delete File");
            console.debug(data);
            alert("파일 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });
};
