/**
 * stating file 을 import 하도록 요청
 * @param dictionaryId
 * @returns {boolean}
 */
const importFiles = function importStagingFilesIntoDictionary(dictionaryId) {
    UploadPollingStatus.start();

    const errorHandle = $("input[name=errorHandle]:checked").val();  // SKIP, STOP

    const fileToImport = $("td.uploadFileName").toArray().map(tdElement => tdElement.innerText);
    $("td.uploadState").toArray().forEach(tdElement => tdElement.innerText = "진행중...");

    console.debug(errorHandle, {fileNames: fileToImport.toString()});

    // dictionaryId 로 import 요청
    $.ajax({
        type: "POST",
        url: `${contextPath}/entryRest/${dictionaryId}/entry/import`,
        data: {errorHandle: errorHandle.toUpperCase()},
        beforeSend: function (jqXHR, settings) {
            console.debug(errorHandle, settings);
        },
        success: function (response) {
            console.info(`Succeed to import ${fileToImport.toString()}`);
            alert("사전 업로드가 완료되었습니다. 결과를 확인해주세요");
        },
        error: function (jqXHR, textStatus, errorThrown) {
            UploadPollingStatus.stop();

            console.debug(jqXHR);
            console.error(`Failed to import ${fileToImport.toString()}`);
            switch (jqXHR.status) {
                case 403:
                    console.error("해당 사전의 업로드 권한이 없습니다.");
                    alert("해당 사전의 업로드 권한이 없습니다.");
                    break;
                case 404:
                    console.error("업로드할 파일이 없습니다.");
                    alert("업로드할 파일이 없습니다.");
                    break;
                case 409:
                    console.error(jqXHR.responseText);
                    alert(`사전 업로드 실패. [${jqXHR.responseText}]`);
                    break;
                default:
                    console.error("Failed to update Dictionary");
                    alert("사전 업로드 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        },
        complete: function (response) {
            console.debug("complete : ", response);
            location.reload();
        }
    });
};

/**
 * Dictionary 파일 업로드
 * @param {Number} dictionaryId Dictionary's id
 * @param {Element} formElement multipart upload form
 */
const uploadFile = function uploadFileIntoImportDictionary(dictionaryId, formElement) {
    const formData = new FormData(formElement);
    const file = formData.get("file");
    const maxFileSize = formElement.dataset["maxFileSize"] || 1024 * 1024 * 100; // 서버 설정 이 안넘어올 경우 100 MB
    const FILE_SIZE_LIMIT = 1024 * 1024 * 1024 * 2; // 2GB

    console.debug(`tm-v3-manager.fileUpload.maxFileSize=${maxFileSize}`);

    try {
        if (!validFileType(file.name)) {
            alert("사용할 수 없는 파일 확장자 입니다.");
            throw `사용할 수 없는 파일 확장자 ${file.name}`;
        }

        if (file.size > maxFileSize) {
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
        console.debug(file);
        formElement.reset();
        return false;
    }

    $.ajax({
        type: "GET",
        url: contextPath + '/dictionaryRest/importHistory?dictionaryId=' + dictionaryId,
        data: {fileName: file.name},
        success: function (response) {
            // 업로드 이력 존재 --> 업로드 금지
            // console.debug(history);
            // alert("업로드 이력이 존재하는 파일입니다.");
            // throw `Already uploaded file : '${file.name}'`;
            
            if (response.result == "S") {
				$.ajax({
	                type: "POST",
	                enctype: "multipart/form-data",
	                url: `${contextPath}/file/import/dictionary/${dictionaryId}/`,
	                data: formData,
	                processData: false,
	                contentType: false,
	                cache: false,
	                // no timeout //timeout: 3 * 60 * 1000, //ms
	                beforeSend: function () {
	                    console.group("file-upload");
	                    console.table({file_name: file.name, file_size: get_file_size(file.size)});
	                    console.debug(formData);
	                    [...document.forms.namedItem("file-upload-form").elements].forEach(e => e.disabled = true);
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
 * 업로드된 Dictionary 파일 제거
 */
const deleteFile = function deleteFileAtImportDictionary(dictionaryId, fileName) {
    if (!fileName) {
        console.error("fileName 확인");
        return false;
    }
    if (!confirm(`${fileName} 을 삭제하시겠습니까?`)) {
        return false;
    }

    $.ajax({
        type: "DELETE",
        url: `${contextPath}/file/import/dictionary/${dictionaryId}/${fileName}`,
        success: function (response) {
            // dictionary 삭제 성공
            console.log(`파일 삭제 성공: ${response}`);

            // alert(`파일=[${fileName}]이 삭제되었습니다.`);

            location.reload(true);
        },
        error: function (data) {
            console.error("Failed to delete File");
            console.debug(data);
            alert("파일 삭제 실패. 서버 연결 상태 및 로그를 확인하세요.");
        }
    });
};

/**
 * 업로드 히스토리 팝업을 띄워서 디테일 로그를 보여준다.
 * @param {DOMStringMap} dataset
 */
const historyDictionaryPopup = function popupUploadHistoryDictionaryEachFile(id, dataset) {
	
    if (!dataset.hasOwnProperty("resourceid")) {
        throw "check param[resourceId]";
    }
    if (!dataset.hasOwnProperty("filename")) {
        throw "check param[fileName]";
    }
    let elemResultTableBody = document.querySelector("#result-data tbody");

    $.ajax({
        type: "GET",
        url: `${contextPath}/dictionaryRest/importHistoryData?dictionaryId=${dataset.resourceid}`,
        data: {
				importId: id
			  , fileName: dataset.filename
			  },
        success: function (history, status, jqhXHR) {
            // console.group(`Dictionary[id=${dataset.resourceid}]'s Entry upload history of '${dataset.filename}'`);
            // console.table(history.log);
            // console.groupEnd();
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
                a.href = `${contextPath}/dictionaryRest/${dataset.resourceid}/importHistoryLogs?${p.toString()}`;
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
};

/**
 * 파일 확장자 validation
 */
const validFileType = function (file_name) {
    const regexFileExtensions = /^(.*\.(csv|txt)$)/gim; // allow .txt .csv
    // const file_type = document.forms['file-upload-form'].elements['file'].files.item(0).type;
    if (!regexFileExtensions.test(file_name)) {
        return false;
    }
    return true;
};
