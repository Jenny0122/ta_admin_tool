/**
 * Button Element toggle disable <--> able
 */
const toggleBtn = function disabeldButtonElement(btnElement) {
    if (btnElement) {
        if (btnElement.disabled == false) {
            btnElement.disabled = true;
            btnElement.classList.remove("btn_mint");
            btnElement.style.cursor = "wait";
            btnElement.value = "테스트중..";
        } else {
            btnElement.disabled = false;
            btnElement.classList.add("btn_mint");
            btnElement.style.cursor = "";
            btnElement.value = "연결테스트";
        }
    }
};

/**
 * tm-server 연결 테스트
 */
// const testTmServer = function testConnectionTMServer(btnElement, alertSuccess = false) {
//     $.ajax({
//         type: "GET",
//         url: contextPath + "/test/tm-server",
//         timeout: 5000,
//         beforeSend(jqXHR, settings) {
//             toggleBtn(btnElement);
//         },
//         success: function (response) {
//             console.log(`tm-server connection succeed. ${response}`);
//             if (alertSuccess) {
//                 alert("TM Server 연결 성공");
//             }
//         },
//         error: function (jqXHR, textStatus, errorThrown) {
//             switch (jqXHR.status) {
//                 case 503: // SERVICE_UNAVAILABLE
//                     console.error(`tm-server connection refused. ${jqXHR.responseText}`);
//                     break;
//                 default:
//                     break;
//             }
//             alert("TM Server 연결 실패.\n" +
//                 "TM 서버 연결 설정을 확인해 주세요.\n" +
//                 "문제가 계속 될 경우 관리자에게 문의 바랍니다.");
//         },
//         complete() {
//             toggleBtn(btnElement);
//         }
//     });
// };

/**
 * MongoDB 연결 테스트
 * */
// const testMongoDb = function testConnectionMongoDatabase(btnElement, alertSuccess = false) {
//     $.ajax({
//         type: "GET",
//         url: contextPath + "/test/mongo-db",
//         timeout: 2000,
//         beforeSend(jqXHR, settings) {
//             toggleBtn(btnElement);
//         },
//         success: function (response) {
//             console.log(`MongoDB connection succeed. ${response}`);
//             if (alertSuccess) {
//                 alert("MongoDB 연결 성공");
//             }
//         },
//         error: function (jqXHR, textStatus, errorThrown) {
//             switch (jqXHR.status) {
//                 case 503: // SERVICE_UNAVAILABLE
//                     console.error(`MongoDB connection refused. ${jqXHR.responseText}`);
//                     break;
//                 default:
//                     break;
//             }
//             alert("MongoDB 연결 실패.\n" +
//                 "데이터베이스 연결 설정을 확인해 주세요\n" +
//                 "문제가 계속 될 경우 관리자에게 문의 바랍니다.");
//         },
//         complete() {
//             toggleBtn(btnElement);
//         }
//     });
// };

/**
 * 사용자의 개인정보 업데이트
 *  - 현재 비밀번호를 입력해야함
 *  - 성공 여부 관련 없이 강제 새로고침
 * @returns {boolean}
 */
const updateUser = function partialUpdateTMUserInfo(btnElement) {
    btnElement.disabled = true;
    const form = document.forms.namedItem("updateUserForm");

    // if newPassword, confirm value check. else remove
    if (form.userPw.value) {
        const regexPw = /^((?=.*[a-zA-Z0-9])(?=.*[^a-zA-Z0-9]).{8,16})$/;
        if (!regexPw.test(form.userPw.value)) {
            btnElement.disabled = false;
            alert("비밀번호는 특수문자를 최소 하나 포함한 8~16자리 영문 대 소문자, 숫자, 특수문자를 사용하세요.");
            form.userPw.focus();
            return false;
        }

        form.userPw.required = true;
        if (form.userPw.value !== form.newPasswordConfirm.value) {
            btnElement.disabled = false;
            alert("입력한 새 비밀번호가 일치하지 않습니다.");
            form.newPasswordConfirm.focus();
            return false;
        }
    } else {
        form.userPw.disabled = true;
    }
    form.newPasswordConfirm.disabled = true;

    if (form.userEmail.value === form.userEmail.dataset.email) {
        form.userEmail.disabled = true;
    } else {
        // validate email
        if (!form.userEmail.value) {
            alert("이메일을 입력해주세요");
            btnElement.disabled = false;
            form.email.focus();
            return false;
        }

        const regexPseudoEmail = /^\S+@\S+\.\S+$/;
        if (!regexPseudoEmail.test(form.userEmail.value)) {
            alert("이메일이 올바르지 않습니다.");
            btnElement.disabled = false;
            form.userEmail.focus();
            return false;
        }
    }

    if (form.userName.value === form.userName.dataset.name) {
        form.name.disabled = true;
    }

    $.ajax({
        async: false,
        type: "POST",
        url: contextPath + "/user/updateUser",
        data: $(form).serialize(),
        success: function (response, status, jqhXHR) {
            alert(`개인정보가 변경되었습니다.`);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            switch (jqXHR.status) {
                case 400:
                    if (/credential|password/i.test(jqXHR.responseText)) {
                        alert('입력하신 비밀번호를 확인해주세요.');
                    } else {
                        alert(`잘못된 요청입니다.\n${jqXHR.responseText}`);
                    }
                    break;
                case 403:
                    alert("개인정보 수정 권한이 없습니다.");
                    break;
                default:
                    console.debug(jqXHR.status);
                    console.debug(jqXHR.responseJSON.message);
                    console.debug(jqXHR.responseJSON);
                    console.error("Failed to update userInfo");
                    alert("개인정보 수정 실패. 서버 연결 상태 및 로그를 확인하세요.");
                    break;
            }
        },
        complete: function (data) {
            location.reload();
        }
    });

    btnElement.disabled = false;
    return false;
};