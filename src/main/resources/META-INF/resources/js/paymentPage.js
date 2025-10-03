
function updateCountdown() {
    const display = document.getElementById("session-time");
    if (!display) return;
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    display.textContent = minutes + " min. " + (seconds < 10 ? "0" : "") + seconds + " sec.";

    if (remainingSeconds > 0) {
        remainingSeconds--;
    } else {
        clearInterval(timerInterval);
        display.textContent = "Sesiunea a expirat";
    }
}

document.addEventListener("DOMContentLoaded", function () {
    if (typeof remainingSeconds !== "undefined") {
        updateCountdown();
        timerInterval = setInterval(updateCountdown, 1000);
    }
});