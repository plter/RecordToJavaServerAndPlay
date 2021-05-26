(function () {
    let ws = new WebSocket(`ws://${location.host}/savemedia`);

    let player = document.querySelector("#player");
    let btnStartRecord = document.querySelector("#btn-record");
    let btnStopRecord = document.querySelector("#btn-stop");
    let btnPlay = document.querySelector("#btn-play");
    let mediaFileName = undefined;
    let currentMediaStream = undefined;

    let isRecording = false;
    let connected = false;
    let currentMediaRecorder = undefined;

    function setRecording(v) {
        isRecording = v;
        if (v) {
            btnStartRecord.style.display = "none";
            btnStopRecord.style.display = "inline-block";
        } else {
            btnStartRecord.style.display = "inline-block";
            btnStopRecord.style.display = "none";
        }
    }

    function btnStopRecordClickedHandler() {
        if (isRecording) {
            if (currentMediaRecorder) {
                currentMediaRecorder.stop();
                currentMediaRecorder = undefined;
                player.src = null;
                player.srcObject = null;
            }
            setRecording(false);
        }
    }

    function btnStartRecordClickedHandler() {
        ws.send(JSON.stringify({ command: "requestFileName" }));
    }

    function wsClosedHandler(e) {
        console.debug(e);
        connected = false;
    }

    function btnPlayClickedHandler() {
        if (mediaFileName) {
            player.muted = false;
            let url = `/download?filename=${mediaFileName}`;
            player.src = url;
        }
    }

    function wsOnOpenHandler() {
        console.debug("Connected to server");
        connected = true;
    }

    function wsMessageHandler(e) {
        console.debug(e);

        let pkg = JSON.parse(e.data);
        if (pkg.command == "fileNameResponse") {
            mediaFileName = pkg.body;
            startRecordProcess();
        }
    }

    function mrOndataavailableHandler(e) {
        ws.send(e.data);
    }

    function mrStoppedHandler(e) {
        ws.send(JSON.stringify({ command: "stopRecord" }));
    }

    async function startRecordProcess() {
        if (!isRecording) {
            player.muted = true;
            if(!currentMediaStream){
                currentMediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
            }
            player.srcObject = currentMediaStream;
            let r = currentMediaRecorder = new MediaRecorder(currentMediaStream);
            r.ondataavailable = mrOndataavailableHandler;
            r.onstop = mrStoppedHandler;
            r.start(1000);

            setRecording(true);
        }
    }

    function addListeners() {
        ws.onopen = wsOnOpenHandler;
        ws.onclose = wsClosedHandler;
        ws.onmessage = wsMessageHandler;
        btnStopRecord.onclick = btnStopRecordClickedHandler;
        btnStartRecord.onclick = btnStartRecordClickedHandler;
        btnPlay.onclick = btnPlayClickedHandler;
    }

    async function main() {
        addListeners();
        setRecording(false);
    }

    main();
})();