function openPage(pageId, button) {

    document.querySelectorAll(".page")
        .forEach(function (page) {
            page.classList.remove("active-page");
        });

    document.querySelectorAll(".nav-btn")
        .forEach(function (btn) {
            btn.classList.remove("active");
        });

    const page = document.getElementById(pageId);

    if (page) {
        page.classList.add("active-page");
    }

    if (button) {
        button.classList.add("active");
    }

    const sidebar = document.querySelector(".sidebar");

    if (window.innerWidth <= 700 && sidebar) {
        sidebar.classList.remove("show");
    }
}


function openPageByName(pageId) {

    const buttons = document.querySelectorAll(".nav-btn");

    for (let button of buttons) {

        const clickText =
            button.getAttribute("onclick");

        if (clickText &&
            clickText.includes("'" + pageId + "'")) {

            openPage(pageId, button);
            break;
        }
    }
}


function toggleMenu() {

    const sidebar =
        document.querySelector(".sidebar");

    if (sidebar) {
        sidebar.classList.toggle("show");
    }
}


function showSelected() {

    const mode =
        document.getElementById("splitMode");

    const box =
        document.getElementById("selectedBox");

    if (!mode || !box) {
        return;
    }

    if (mode.value === "SELECTED") {
        box.style.display = "block";
    } else {
        box.style.display = "none";
    }
}


document.addEventListener(
    "DOMContentLoaded",
    function () {
        showSelected();
    }
);