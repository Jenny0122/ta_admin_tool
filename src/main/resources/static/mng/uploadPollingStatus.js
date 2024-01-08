/**
 * 비동기 요청이 폴링되고 있는지 체크.
 * 모듈 패턴으로 캠슐화 구현.
 * @type {{start(): void, stop(): void, isInProgress(): boolean}}
 * @see https://codeameba.github.io/2019/05/10/programming/js-no-more-global-variable/
 */
const UploadPollingStatus = (function () {

    // private
    let inProgress = false;

    return {
        /**
         * @returns {boolean} return true if start in progress, else false.
         */
        isInProgress() {
            return inProgress;
        },
        /**
         * sets status as in progress.
         */
        start() {
            inProgress = true;

            // 버튼 비활성화
            [...document.forms.namedItem("file-upload-form").elements]
                .forEach(e => e.disabled = true);
            document.querySelectorAll("tr.tbody-staged i.fa-trash")
                .forEach(e => e.style.display = "none");
            document.getElementsByName("errorHandle")
                .forEach(e => e.disabled = true);
            document.getElementsByName("field")
                .forEach(e => e.disabled = true);
            let btnUpload = document.getElementById("button-upload");
            btnUpload.disabled = true;
            btnUpload.style.cursor = "not-allowed";
        },
        /**
         * sets status as not in progress.
         */
        stop() {
            inProgress = false;

            // 버튼 활성화
            [...document.forms.namedItem("file-upload-form").elements]
                .forEach(e => e.disabled = false);
        },

        /**
         * rest status and button
         */
        reset() {
            inProgress = false;

            // 버튼 비활성화
            [...document.forms.namedItem("file-upload-form").elements]
                .forEach(e => e.disabled = false);
            document.querySelectorAll("tr.tbody-staged i.fa-trash")
                .forEach(e => e.style.display = "");
            document.getElementsByName("errorHandle")
                .forEach(e => e.disabled = false);
            document.getElementsByName("field")
                .forEach(e => e.disabled = false);
            let btnUpload = document.getElementById("button-upload");
            btnUpload.disabled = false;
            btnUpload.style.cursor = "";
        }
    };

}());
