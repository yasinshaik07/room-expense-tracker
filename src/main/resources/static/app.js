function openPage(pageId, button) {

    const pages =
        document.querySelectorAll(".page");

    pages.forEach(function (page) {
        page.classList.remove("active-page");
    });


    const buttons =
        document.querySelectorAll(".nav-btn");

    buttons.forEach(function (btn) {
        btn.classList.remove("active");
    });


    const selectedPage =
        document.getElementById(pageId);

    if (selectedPage) {
        selectedPage.classList.add("active-page");
    }


    if (button) {
        button.classList.add("active");
    }


    const sidebar =
        document.querySelector(".sidebar");

    if (
        window.innerWidth <= 760 &&
        sidebar
    ) {
        sidebar.classList.remove("show");
    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


function openPageByName(pageId) {

    const buttons =
        document.querySelectorAll(".nav-btn");

    for (let button of buttons) {

        const clickText =
            button.getAttribute("onclick");

        if (
            clickText &&
            clickText.includes("'" + pageId + "'")
        ) {

            openPage(
                pageId,
                button
            );

            return;
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

    const splitMode =
        document.getElementById("splitMode");

    const selectedBox =
        document.getElementById("selectedBox");

    if (
        !splitMode ||
        !selectedBox
    ) {
        return;
    }


    if (
        splitMode.value === "SELECTED"
    ) {

        selectedBox.style.display =
            "block";

    } else {

        selectedBox.style.display =
            "none";
    }
}


document.addEventListener(
    "DOMContentLoaded",
    function () {

        showSelected();


        document.addEventListener(
            "click",
            function (event) {

                const sidebar =
                    document.querySelector(".sidebar");

                const menuButton =
                    document.querySelector(".menu-btn");


                if (
                    window.innerWidth <= 760 &&
                    sidebar &&
                    sidebar.classList.contains("show")
                ) {

                    if (
                        !sidebar.contains(event.target) &&
                        menuButton &&
                        !menuButton.contains(event.target)
                    ) {

                        sidebar.classList.remove("show");
                    }
                }
            }
        );
    }
);